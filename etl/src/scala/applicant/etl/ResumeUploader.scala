package applicant.etl

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.apache.tika.sax.WriteOutContentHandler
import java.io._
import scopt.OptionParser
import org.elasticsearch.spark._
import org.apache.commons.io.FilenameUtils
import org.apache.commons.codec.binary.{Hex, Base64}
import applicant.nlp._
import java.security.MessageDigest

import scala.collection.mutable.{ListBuffer, Map, LinkedHashMap, HashMap}

/**
 *@author Brantley Gilbert
 *
 *@version 0.0.1
 *
 */

object ResumeUploader {

  //Class to store command line options
  case class Command(sourceDirectory: String = "", sparkMaster: String = "",
    esNodes: String = "", esPort: String = "", esAppIndex: String = "",
    nlpRegex: String = "", nlpModels: String = "", esAttIndex: String = "",
    fromES: Boolean = false, uploadindex: String = "")

  /**
   * Will itialize the spark objects and pass off files to tika
   */
  def uploadResumes(options: Command) {
    //File path from the command line, uses wildcard to open all files
    val filesPath = options.sourceDirectory + "*"
    //Create Spark configuration object, with Elasticsearch configuration
    val conf = new SparkConf().setMaster(options.sparkMaster)
      .setAppName("ResumeUploader").set("es.nodes", options.esNodes)
      .set("es.port", options.esPort)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    /*
      The internal hostname is ip-172-31-61-189.ec2.internal (172.31.61.189).  Internally the REST API is available on port 9200 and the native transport runs on port 9300.
    */

    //Create Spark RDD using conf
    val sc = new SparkContext(conf)
    println("Loading uploads into RDD")
    val fileData = getUploadBytes(options.uploadindex, sc)
    // Create EntityExtractor object
    val models = options.nlpModels.split(",")
    val patterns = options.nlpRegex
    println("RDD created, Creating EntityExtractor")
    val extractor = new EntityExtractor(models, patterns)
    println("Broadcasting EntityExtractor")
    val broadcastExtractor = sc.broadcast(extractor)

    var fileCount = sc.accumulator(0)
    println("Starting parsing")
    fileData.map { currentFile =>

      val esId = Hex.encodeHexString(MessageDigest.getInstance("MD5").digest(currentFile)).toLowerCase()
      val stream : ByteArrayInputStream = new ByteArrayInputStream(currentFile)

      println("Parsing applicant " + esId + ", " + fileCount + " files parsed")
      val text = TextExtractor.extractText(stream)

      fileCount += 1

      broadcastExtractor.synchronized {
        val entitySet = broadcastExtractor.value.extractEntities(text)
        val app = ApplicantData(entitySet, esId, text)
        app.toMap()
      }

    }.saveToEs(options.esAppIndex + "/applicant", Map("es.mapping.id" -> "id"))

    var pdfCount = sc.accumulator(0)

    fileData.map{ currentFile =>
      val esId = Hex.encodeHexString(MessageDigest.getInstance("MD5").digest(currentFile.toArray)).toLowerCase()
      val stream : ByteArrayInputStream = new ByteArrayInputStream(currentFile)

      println("Uploading pdf " + esId + ", " + pdfCount + " pdfs uploaded")
      pdfCount += 1
      Map(
        "hash" -> esId,
        "applicantid" -> esId,
        "base64string" -> currentFile,
        "filename" -> "testing",
        "extension" -> "testing",
        "metadata" -> TextExtractor.extractMetadata(stream)
        )

    }.saveToEs(options.esAttIndex + "/attachment", Map("es.mapping.id" -> "hash"))

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
            uploadResumes(options)
        //Elsewise, just exit
        case None =>
    }
  }

  /**
   * Reads resumes from the uploads index in Elasticsearch
   * Note: may be improved by using spark streaming
   * @param uploadIndex Elasticsearch upload index
   * @param sc SparkContext
   * @return An RDD of data streams from Elasticsearch
   */
  def getUploadBytes(uploadIndex: String, sc: SparkContext) : RDD[Array[Byte]] = {
    val uploadMapRDD = sc.esRDD(uploadIndex + "/upload").values
    val streamRdd = uploadMapRDD.map{ resume =>
      val base64 = getString(resume("base64string"))
      Base64.decodeBase64(base64)
    }
    return streamRdd
  }
/*
  private def base64ToStream (base64: String) : DataInputStream = {
    val bytes = Base64.decodeBase64(base64)
    val stream : ByteArrayInputStream = new ByteArrayInputStream(bytes)
    return new DataInputStream(stream)
  }
*/
  private def getString(value: AnyRef): String = {
    if (value == None) {
      return ""
    }
    return value.asInstanceOf[String]
  }

}
