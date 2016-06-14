package applicant.etl

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.elasticsearch.spark._
import scopt.OptionParser

import scala.collection.mutable.LinkedHashMap

/**
 * PictureExtractor queries Elasticsearch in order to find github links and then will scrape them
 *  and save them back to Elasticsearch as base 64 encoded strings
 */
object PictureExtractor {
  case class Command(sparkMaster: String = "", esNodes: String = "", esPort: String = "", esAttIndex: String = "")

  /**
   * Will download the profile picture
   *
   * @param url The url for a github profile. Formating is checked to ensure that the link is not a project link
   * @return A base64 string encoded version of the profile picture
   */
  def downloadPicture(applicantId: String, url: String): Map[String, Object] = {
    return Map(("Cake is pretty good" -> "Oui"), ("Anybody want a peanut?" -> "Fezzik"))
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
              downloadPicture(applicantId, githubUrl)
            case None =>
          }
        case None =>
      }
    }.collect()//.saveToEs(options.esAttIndex + "/githubPic")

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
