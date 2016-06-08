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
import applicant.nlp._

import scala.collection.mutable.{ListBuffer, Map, LinkedHashSet}

/**
 *@author Brantley Gilbert
 *
 *@version 0.0.1
 *
 */

object ResumeReader {

  //Class to store command line options
  case class Command(sourceDirectory: String = "", sparkMaster: String = "", esNodes: String = "", esPort: String = "")

  /**
   * Uses Apache Tika library to parse out text from a PDF
   *
   *@param data A PortableDataStream from Spark of a PDF file
   */
  def extractText (data: PortableDataStream) : String = {

    // Apache Tika parser object, auto detects file type
    val myparser : AutoDetectParser = new AutoDetectParser()
    // Input stream for parser, from PortableDataStream data
    val stream : InputStream = data.open()
    // Creates object to hold text ouput from Tika parser
    val handler : WriteOutContentHandler = new WriteOutContentHandler(-1)
    // Creates a object to hold the metadata of the file being parsed
    val metadata : Metadata = new Metadata()
    // Object to pass context information to Tika parser, use to modify parser
    val context : ParseContext = new ParseContext()


    try {
      // Parse text from file and store in hander object
      myparser.parse(stream, handler, metadata, context)
    }
    finally {
      // Close stream after parsing
      stream.close
    }

    return handler.toString()
  }

  /**
   * Will itialize the spark objects and pass off files to tika
   */
  def parseResumes(options: Command) {
    //File path from the command line, uses wildcard to open all files
    val filesPath = options.sourceDirectory + "*"
    //Create Spark configuration object, with Elasticsearch configuration
    val conf = new SparkConf().setMaster(options.sparkMaster)
      .setAppName("ResumeReader").set("es.nodes", options.esNodes)
      .set("es.port", options.esPort)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    /*
      The internal hostname is ip-172-31-61-189.ec2.internal (172.31.61.189).  Internally the REST API is available on port 9200 and the native transport runs on port 9300.
    */

    //Create Spark RDD using conf
    val sc = new SparkContext(conf)
    //Create a key-value pair RDD of files within resume directory
    //RDD is an array of tuples (String, PortableDataStream)
    val fileData = sc.binaryFiles(filesPath)

    // Create EntityGrabber object
    val models = List[String]("model/nlp/en-ner-degree.bin", "model/nlp/en-ner-location.bin", "model/nlp/en-ner-organization.bin", "model/nlp/en-ner-person.bin", "model/nlp/en-ner-school.bin", "model/nlp/en-ner-title.bin")
    val patterns = "model/nlp/regex.txt"
    val grabber = new EntityGrabber(models, patterns)

    val broadcastGrabber = sc.broadcast(grabber)

    fileData.values.map { currentFile =>
      var entitySet: LinkedHashSet[(String, String)] = null
      val text = extractText(currentFile)

      broadcastGrabber.synchronized {
        entitySet = broadcastGrabber.value.extractEntities(text)
        EntityMapper.createMap(entitySet, FilenameUtils.getBaseName(currentFile.getPath()), text)
      }

    }.saveToEs("map_test/test", Map("es.mapping.id" -> "id", "es.mapping.exclude" -> "id"))

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
    val parser = new OptionParser[Command]("ResumeReader") {
        opt[String]('d', "directory") required() valueName("<directory>") action { (x, c) =>
            c.copy(sourceDirectory = x)
        } text ("Path to resumes")
        opt[String]('m', "master") required() valueName("<master>") action { (x, c) =>
            c.copy(sparkMaster = x)
        } text ("Spark master argument.")
        opt[String]('n', "nodes") required() valueName("<nodes>") action { (x, c) =>
            c.copy(esNodes = x)
        } text ("list of Elasticsearch nodes to connect to, usually IP address of ES server.")
        opt[String]('p', "port") required() valueName("<port>") action { (x, c) =>
            c.copy(esPort = x)
        } text ("Default HTTP/REST port used for connecting to Elasticsearch, usually 9200.")

        note ("Reades through a directory of resumes and parses the text from each.\n")
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
}
