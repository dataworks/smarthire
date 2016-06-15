package applicant.etl

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
  case class Command(sparkMaster: String = "", esNodes: String = "", esPort: String = "", esAttIndex: String = "")


  /**
   * Will clean up a raw url in order to get a link to the user's profile picture
   *
   * @param rawUrl The url pulled straight from the user's resume
   */
  def cleanGithubUrl(rawUrl: String): Option[String] = {
    rawUrl match {
      case url if url.startsWith("https://github.com/") =>
        var slashCount = 0
        val urlBuilder = new StringBuilder()

        var slashedUrl = url.substring(19)

        if (!slashedUrl.endsWith("/")) {
          slashedUrl += "/"
        }

        //Add the github content url
        urlBuilder.append("https://avatars.githubusercontent.com/")

        //Grab each character up to the slash
        for (c <- slashedUrl; if slashCount < 1) {
          urlBuilder.append(c)

          if (c.equals('/')) {
            slashCount += 1
          }
        }

        //remove the trailing '/'
        urlBuilder.setLength(urlBuilder.length - 1)

        return Some(urlBuilder.toString())
      case _ =>
        return None
    }
  }


  /**
   * Will download the profile picture
   *
   * @param url The url for a github profile. Formating is checked to ensure that the link is not a project link
   * @return A base64 string encoded version of the profile picture
   */
  def downloadPicture(applicantId: String, githubUrl: String): Map[String, Object] = {
      val url = new URL(githubUrl)

      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")
      var in: InputStream = connection.getInputStream //< ------------------- Use TextExtractor to pull meta data and other info

      val byteArray = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray

      var out: BufferedOutputStream = new BufferedOutputStream(new FileOutputStream(applicantId + ".png"))
      out.write(byteArray)
      out.close()
      in.close()

      return Map(
        "hash" -> MessageDigest.getInstance("MD5").digest(byteArray),
        "applicantid" -> applicantId,
        "base64string" -> byteArray,
        "filename" -> FilenameUtils.getName(githubUrl),
        "extension" -> FilenameUtils.getExtension(githubUrl),
        "metadata" -> TextExtractor.extractMetadata(in)
        )

  }

  /**
   * Will query Elasticsearch for github pictures, nab them, and push them back to elasticsearch
   *
   * @param options The command line options
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
               cleanGithubUrl(githubUrl) match {
                case Some(properUrl) =>
                  println(downloadPicture(applicantId, properUrl))
                case None =>
              }
            case None =>
          }
        case None =>
      }
    }.saveToEs(options.esAttIndex + "/githubPic")

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
