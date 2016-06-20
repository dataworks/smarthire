package applicant.etl

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
import org.apache.commons.codec.binary.Base64
import applicant.nlp._
import java.security.MessageDigest

import scala.collection.mutable.{ListBuffer, Map, LinkedHashMap, HashMap}
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}

/**
 *@author Brantley Gilbert
 *
 *@version 0.0.1
 *
 */

object ResumeParser {

  //Class to store command line options
  case class Command(sourceDirectory: String = "", sparkMaster: String = "",
    esNodes: String = "", esPort: String = "", esAppIndex: String = "",
    nlpRegex: String = "", nlpModels: String = "", esAttIndex: String = "")

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
    val input = sc.textFile("data/txt").map(line => new LuceneTokenizer().tokenize(line))
    val w2vModel = Word2VecModel.load(sc, "model/w2v")
    val w2vMap = w2vSynonymMapper(w2vModel, List("Java","Hadoop", "Spark"), 50)

    //Create a key-value pair RDD of files within resume directory
    //RDD is an array of tuples (String, PortableDataStream)
    val fileData = sc.binaryFiles(filesPath)

    // Create EntityExtractor object
    val models = options.nlpModels.split(",")
    val patterns = options.nlpRegex
    val extractor = new EntityExtractor(models, patterns)


    val broadcastExtractor = sc.broadcast(extractor)

    var fileCount: Int = 0

    fileData.values.map { currentFile =>
      println("Parsing applicant " + FilenameUtils.getBaseName(currentFile.getPath()) + ", " + fileCount + " files parsed")
      val text = TextExtractor.extractText(currentFile.open())

      fileCount += 1
      broadcastExtractor.synchronized {
        val entitySet = broadcastExtractor.value.extractEntities(text)
        EntityRecord.create(entitySet, FilenameUtils.getBaseName(currentFile.getPath()), text)
      }

    }.saveToEs(options.esAppIndex + "/applicant", Map("es.mapping.id" -> "id"))

    fileData.values.map{ currentFile =>
      Map(
        "hash" -> MessageDigest.getInstance("MD5").digest(currentFile.toArray),
        "applicantid" -> FilenameUtils.getBaseName(currentFile.getPath()),
        "base64string" -> currentFile.toArray,
        "filename" -> FilenameUtils.getName(currentFile.getPath()),
        "extension" -> FilenameUtils.getExtension(currentFile.getPath()),
        "metadata" -> TextExtractor.extractMetadata(currentFile.open())
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

  /**
   * Creates a hash map of w2v synonyms and booleans
   * @param model A w2v model generated from source data
   * @param terms The search terms to find synonyms for
   * @param synonymCount Number of synonyms to return per term
   * @return A hash map of the synonyms as keys and booleans as values
   */
  def w2vSynonymMapper(model: Word2VecModel, terms: List[String], synonymCount: Int) : HashMap[String,Boolean] = {
    val map = HashMap.empty[String,Boolean]
    terms.foreach{ term =>
      val synonyms = model.findSynonyms(term.toLowerCase(), synonymCount)
      for((synonym, cosineSimilarity) <- synonyms) {
        map += (synonym -> false)
      }
    }
    return map
  }
}
