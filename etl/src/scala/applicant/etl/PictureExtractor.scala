package applicant.etl

import scala.language.postfixOps
import scala.io._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.elasticsearch.spark._
import scopt.OptionParser
import java.security.MessageDigest
import org.apache.commons.io.FilenameUtils

import scala.collection.mutable.LinkedHashMap

import java.net.{URL, HttpURLConnection}
import java.io._

/**
 * PictureExtractor queries Elasticsearch in order to find github links and then will scrape them
 *  and save them back to Elasticsearch as base 64 encoded strings
 */
object PictureExtractor {
  case class Command(sparkMaster: String = "", esNodes: String = "", esPort: String = "", esAttIndex: String = "", picDirectories: String = "", githubPics: Boolean = false)

  /**
   * Will download the profile picture from github
   *
   * @param url The url for a github profile. Formating is checked to ensure that the link is not a project link
   * @return A base64 string encoded version of the profile picture
   */
  def downloadGithubPicture(applicantId: String, urlStr: String): Map[String, Object] = {
    try {
      val url = new URL(urlStr)

      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")
      var in: InputStream = connection.getInputStream

      val byteArray = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray

      in.close()

      val metadataMap = TextExtractor.extractMetadata(new ByteArrayInputStream(byteArray))
      var fileExtension: String = ""
      metadataMap.get("Content-Type") match {
        case Some(jpg) if jpg.endsWith("jpeg") =>
          fileExtension = "jpg"
        case Some(png) if png.endsWith("png") =>
          fileExtension = "png"
        case _ =>
          return Map()
      }

      return Map(
        "hash" -> MessageDigest.getInstance("MD5").digest(byteArray),
        "applicantid" -> applicantId,
        "base64string" -> byteArray,
        "filename" -> (FilenameUtils.getName(urlStr) + "." + fileExtension),
        "extension" -> fileExtension,
        "metadata" -> metadataMap
        )
    }
    catch {
      case ex: Exception => return Map()
    }
  }

  /**
   * Will query Elasticsearch for github pictures, nab them, and push them back to elasticsearch in the attachments index
   *
   * @param options The command line options
   */
  def getGithubPictures(sc: SparkContext, options: Command) {

    //query Elasticsearch for github
    val githubApplicants = sc.esRDD("applicants/applicant", "?q=contact.github:http*")

    githubApplicants.map { applicant =>
      val applicantId = applicant._1
      val contactOption = applicant._2.get("contact")
      contactOption match {
        case Some(contact) =>
          val githubOption = contact.asInstanceOf[LinkedHashMap[String, String]].get("github")
          githubOption match {
            case Some(githubUrl) =>
               LinkParser.parseGithubProfile("https://avatars.githubusercontent.com/", githubUrl) match {
                case Some(properUrl) =>
                  downloadGithubPicture(applicantId, properUrl)
                case None =>
              }
            case None =>
          }
        case None =>
      }
    }.saveToEs(options.esAttIndex + "/attachment", Map("es.mapping.id" -> "hash"))
  }

  /**
   * Will upload pictures to elasticsearch with their metadata
   * Since ids in elasticsearch are base-64 hashes, we have saved the pictures with _ instead of /, so must convert back
   *
   * @param options The command line options
   * @param sc The spark context
   */
  def loadFromDirectory(sc: SparkContext, options: Command, directory: String) {
    //Create a key-value pair RDD of files within resume directory
    //RDD is an array of tuples (String, PortableDataStream)
    val fileData = sc.binaryFiles(directory)

    fileData.keys.map { test =>
      println(test)
    }

    fileData.values.map { currentFile =>

      Map(
        "hash" -> MessageDigest.getInstance("MD5").digest(currentFile.toArray),
        "applicantid" -> FilenameUtils.getBaseName(currentFile.getPath()).replace("_","/"),
        "base64string" -> currentFile.toArray,
        "filename" -> FilenameUtils.getName(currentFile.getPath()),
        "extension" -> FilenameUtils.getExtension(currentFile.getPath()),
        "metadata" -> TextExtractor.extractMetadata(currentFile.open())
        )

    }.saveToEs(options.esAttIndex + "/attachment", Map("es.mapping.id" -> "hash"))
  }

  /**
   *  Starts spark and checks options for what types of pictures to load
   *
   */
  def getPictures(options: Command) {
    val conf = new SparkConf().setMaster(options.sparkMaster)
      .setAppName("ResumeParser").set("es.nodes", options.esNodes)
      .set("es.port", options.esPort)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    /*
      The internal hostname is ip-172-31-61-189.ec2.internal (172.31.61.189).  Internally the REST API is available on port 9200 and the native transport runs on port 9300.
    */

    //Create Spark RDD using conf
    val sc = new SparkContext(conf)

    //Check to see if we need to load github pictures
    if (options.githubPics == true) {
      println("Loading pictures from GitHub...")
      getGithubPictures(sc, options)
      println("Github pictures loaded")
    }

    //Check to see if we need to load pictures from a directory
    if (options.picDirectories != "") {
      val directories = options.picDirectories.split(",")
      for (directory <- directories) {
        println("Loading pictures from " + directory + "...")
        loadFromDirectory(sc, options, directory)
        println("Directory pictures have finished loading")
      }
    }

    sc.stop()
  }

  def main(args: Array[String]) {
    //Command line option parser
    val parser = new OptionParser[Command]("ResumeParser") {
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
        opt[String]('d', "picturedirectories") valueName("<picturedirectories>") action { (x, c) =>
            c.copy(picDirectories = x)
        } text ("A sequence of comma separated directories with uploadable pictures inside")
        opt[Unit]('g', "github") action { (_, c) =>
            c.copy(githubPics = true)
        } text("A flag used to specify loading github pictures from elasticsearch applicants")

        note ("Queries github links from Elasticsearch and scrapes profile pictures")
        help("help") text("Prints this usage text")
    }

    // Parses command line arguments and passes them to the search
    parser.parse(args, Command()) match {
      //If the command line options were all present continue
      case Some(options) =>
        getPictures(options)
      //Elsewise, just exit
      case None =>
    }
  }
}
