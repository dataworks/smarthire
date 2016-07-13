package applicant.etl

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import java.io._
import scopt.OptionParser
import org.elasticsearch.spark._
import org.apache.commons.io.FilenameUtils
import org.apache.commons.codec.binary.Base64
import applicant.nlp._

import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.{Map, HashMap, LinkedHashMap}

object ResumeParser {

  //Class to store command line options
  case class Command(sourceDirectory: String = "", sparkMaster: String = "",
    esNodes: String = "", esPort: String = "", esAppIndex: String = "",
    nlpRegex: String = "", nlpModels: String = "", esAttIndex: String = "",
    fromES: Boolean = false, uploadindex: String = "")

  //logger
  val log: Logger = LoggerFactory.getLogger(getClass())

  /**
   * Will itialize the spark objects and pass off files to tika
   */
  def parseResumes(options: Command) {
    //File path from the command line, uses wildcard to open all files
    val filesPath = options.sourceDirectory + "*"
    //Create Spark configuration object, with Elasticsearch configuration
    val conf = new SparkConf().setMaster(options.sparkMaster)
      .setAppName("ResumeParser").set("es.nodes", options.esNodes)
      .set("es.port", options.esPort)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    /*
      The internal hostname is ip-172-31-61-189.ec2.internal (172.31.61.189).  Internally the REST API is available on port 9200 and the native transport runs on port 9300.
    */

    //Create Spark RDD using conf
    val sc = new SparkContext(conf)

    //Query elasticsearch for the applicants who have an email. This will be used to ensure that duplicate resumes are not saved.
    val emailRDD = sc.esRDD("applicants/applicant", "?q=contact.email:*")

    val emailArray = emailRDD.map { applicantMap =>
      val currentApplicant = ApplicantData(applicantMap._2)
      (currentApplicant.email, currentApplicant.applicantid)
    }.collect()

    val emails = HashMap.empty[String, String]

    for (emailPair <- emailArray) {
      emails += emailPair
    }

    log.debug(emails.size + " emails were queried from elasticsearch")

    //load the RDD of data from either the local drive or from elasticsearch
    val fileData = if (options.fromES) {
      sc.esRDD(options.uploadindex + "/upload", "?q=processed:false").values.map{ resume =>
        ResumeData(getString(resume("name")),Base64.decodeBase64(getString(resume("base64string"))), new DataInputStream(new ByteArrayInputStream(Base64.decodeBase64(getString(resume("base64string"))))),getString(resume("id")))
      }
    }
    else {
      sc.binaryFiles(filesPath).values.map{ resume =>
        ResumeData(FilenameUtils.getName(resume.getPath()),resume.toArray,resume.open,"")
      }
    }

    // Create EntityExtractor object
    val models = options.nlpModels.split(",")
    val patterns = options.nlpRegex
    val extractor = new EntityExtractor(models, patterns)

    val broadcastExtractor = sc.broadcast(extractor)

    var fileCount = sc.accumulator(0)

    //Go through the set of pdfs and parse out info
    val parsedData = fileData.map { resume =>
      log.info("Parsing applicant " + resume.esId + ", " + fileCount + " files parsed")
      fileCount += 1

      var entitySet: LinkedHashMap[(String, String),(String, String)] = null
      this.synchronized {
        entitySet = broadcastExtractor.value.extractEntities(resume.text)
      }

      val app = ApplicantData(entitySet, resume.esId, resume.text)

      var specialOldHash: (String, String) = null
      if (emails.contains(app.email)) {
        //add the old and new applicantid to a pair so that it can be used later when saving the resume data
        val oldApplicantId = emails(app.email)
        specialOldHash = (app.applicantid -> oldApplicantId)
        log.debug(app.applicantid + " already has an email in the system, their id will be changed to " + oldApplicantId)

        app.applicantid = oldApplicantId
      }

      (app.toMap() -> specialOldHash)
    }

    //Save the applicant maps to Elasticsearch
    parsedData.map(_._1).saveToEs(options.esAppIndex + "/applicant", Map("es.mapping.id" -> "id"))

    //Add the hashes of the applicants who are already in elasticsearch to a map
    val oldHashMapping = HashMap.empty[String, String]
    val oldHashes = parsedData.map(_._2).filter(_ != null).collect()

    for (pair <- oldHashes) {
      oldHashMapping += pair
    }

    //Upload the resumes to elasticsearch
    var pdfCount = sc.accumulator(0)

    fileData.map{ resume =>
      log.info("Parsing applicant " + resume.esId + ", " + pdfCount + " files parsed")
      pdfCount += 1

      val newId = if (oldHashMapping.contains(resume.esId)) oldHashMapping(resume.esId) else resume.esId

      Map(
        "hash" -> newId,
        "applicantid" -> newId,
        "base64string" -> resume.base64string,
        "filename" -> resume.filename,
        "extension" -> resume.extension,
        "metadata" -> resume.metaDataMap
        )
    }.saveToEs(options.esAttIndex + "/attachment", Map("es.mapping.id" -> "hash"))

    // Set uploads processed to true after processing if we pulled from ES
    if (options.fromES) {
      fileData.map{ resume =>
        Map(
          "id" -> resume.uploadId,
          "type" -> "upload",
          "name" -> resume.filename,
          "base64string" -> resume.base64string,
          "processed" -> true
        )
      }.saveToEs(options.uploadindex + "/upload", Map("es.mapping.id" -> "id"))
    }

    sc.stop()

  }

  def main(args: Array[String]) {

  /**
   * Main method
   * <p>
   * Spark job to read all PDF files in a directory and use
   * Apache Tika to parse out the text.  Needed to extract
   * usable text from PDF resumes
   * <p>
   *
   * @param args Array of Strings: <Resume Directory> <Spark Master>
   */

    //Command line option parser
    val parser = new OptionParser[Command]("ResumeParser") {
        opt[String]('d', "directory") required() valueName("<directory>") action { (x, c) =>
            c.copy(sourceDirectory = x)
        } text ("Path to resumes.")
        opt[String]('m', "master") required() valueName("<master>") action { (x, c) =>
            c.copy(sparkMaster = x)
        } text ("Spark master argument.")
        opt[String]('n', "nodes") required() valueName("<nodes>") action { (x, c) =>
            c.copy(esNodes = x)
        } text ("Elasticsearch node to connect to, usually IP address of ES server.")
        opt[String]('p', "port") required() valueName("<port>") action { (x, c) =>
            c.copy(esPort = x)
        } text ("Default HTTP/REST port used for connecting to Elasticsearch, usually 9200.")
        opt[String]('i', "applicantindex") required() valueName("<applicantindex>") action { (x, c) =>
            c.copy(esAppIndex = x)
        } text ("Name of the Elasticsearch index to save applicant data to.")
        opt[String]('a', "attachmentindex") required() valueName("<attachmentindex>") action { (x, c) =>
            c.copy(esAttIndex = x)
        } text ("Name of the Elasticsearch index to save attachment data to.")
        opt[String]('r', "regex") required() valueName("<regex>") action { (x, c) =>
            c.copy(nlpRegex = x)
        } text ("Location of the regex file for OpenNLP, usually named regex.txt")
        opt[String]('o', "models") required() valueName("<models>") action { (x, c) =>
            c.copy(nlpModels = x)
        } text ("Path to the binary models for OpenNLP, comma delimited")
        opt[String]('u', "uploadindex") required() valueName("<uploadindex>") action { (x, c) =>
            c.copy(uploadindex = x)
        } text ("Name of the Elasticsearch upload index to pull resumes from")
        opt[Unit]('f', "fromElasticsearch") action { (_, c) =>
            c.copy(fromES = true)
        } text("A flag used to specify loading resumes from elasticsearch uploads")

        note ("Reades through a directory of resumes, parses the text from each, and saves the applicant data to Elasticsearch\n")
        help("help") text("Prints this usage text")
    }

    // Parses command line arguments and passes them to the search
    parser.parse(args, Command()) match {
        //If the command line options were all present continue
        case Some(options) =>
            //Read all of the files in sourceDirectory and use Tika to grab the text from each
            parseResumes(options)
        //Elsewise, just exit
        case None =>
    }

  }

  private def getString(value: AnyRef): String = {
    if (value == None) {
      return ""
    }
    return value.asInstanceOf[String]
  }

}
