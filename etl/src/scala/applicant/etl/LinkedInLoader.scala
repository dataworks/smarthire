package applicant.etl

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.elasticsearch.spark._
import scopt.OptionParser
import org.apache.commons.io.FilenameUtils
import org.apache.commons.codec.binary.Base64

import java.security.MessageDigest

/**
 * LinkedInLoader will look in a directory of LinkedIn profile pictures
 *  and upload them to elasticsearch as base64 strings
 */
object LinkedInLoader {
  case class Command(picDirectory: String = "", sparkMaster: String = "", esNodes: String = "", esPort: String = "", esAttIndex: String = "")

  /**
   * Will upload pictures to elasticsearch with their metadata
   *
   * @param options The command line options
   */
  def load(options: Command) {
    val conf = new SparkConf().setMaster(options.sparkMaster)
      .setAppName("LinkedInLoader").set("es.nodes", options.esNodes)
      .set("es.port", options.esPort)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    /*
      The internal hostname is ip-172-31-61-189.ec2.internal (172.31.61.189).  Internally the REST API is available on port 9200 and the native transport runs on port 9300.
    */

    //Create Spark RDD using conf
    val sc = new SparkContext(conf)

    //Create a key-value pair RDD of files within resume directory
    //RDD is an array of tuples (String, PortableDataStream)
    val fileData = sc.binaryFiles(options.picDirectory)

    fileData.values.map { currentFile =>
      Map(
        "hash" -> MessageDigest.getInstance("MD5").digest(currentFile.toArray),
        "applicantid" -> FilenameUtils.getBaseName(currentFile.getPath()),
        "base64string" -> currentFile.toArray,
        "filename" -> FilenameUtils.getName(currentFile.getPath()),
        "extension" -> FilenameUtils.getExtension(currentFile.getPath()),
        "metadata" -> TextExtractor.extractMetadata(currentFile.open())
        )
    }.saveToEs(options.esAttIndex + "/attachment", Map("es.mapping.id" -> "id"))

    sc.stop()
  }

  def main(args: Array[String]) {
    //Command line option parser
    val parser = new OptionParser[Command]("ResumeParser") {
        opt[String]('d', "directory") required() valueName("<directory>") action { (x, c) =>
            c.copy(picDirectory = x)
        } text ("Directory to LinkedIn pictures")
        opt[String]('m', "master") required() valueName("<master>") action { (x, c) =>
            c.copy(sparkMaster = x)
        } text ("Spark master argument.")
        opt[String]('n', "nodes") required() valueName("<nodes>") action { (x, c) =>
            c.copy(esNodes = x)
        } text ("Elasticsearch node to connect to, usually IP address of ES server.")
        opt[String]('p', "port") required() valueName("<port>") action { (x, c) =>
            c.copy(esPort = x)
        } text ("Default HTTP/REST port used for connecting to Elasticsearch, usually 9200.")
        opt[String]('a', "attachmentindex") required() valueName("<attachmentindex>") action { (x, c) =>
            c.copy(esAttIndex = x)
        } text ("Name of the Elasticsearch index to save attachment data to.")

        note ("Queries github links from Elasticsearch and scrapes profile pictures")
        help("help") text("Prints this usage text")
    }

    // Parses command line arguments and passes them to the search
    parser.parse(args, Command()) match {
      //If the command line options were all present continue
      case Some(options) =>
        load(options)
      //Elsewise, just exit
      case None =>
    }
  }
}
