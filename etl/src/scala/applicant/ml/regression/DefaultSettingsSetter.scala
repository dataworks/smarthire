package applicant.ml.regression

import scopt.OptionParser

import org.apache.spark.SparkContext
import org.slf4j.{Logger, LoggerFactory}
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.elasticsearch.spark._

/**
 * A class that sets the elasticsearch setting index to turn on every feature, use tech keywords,
 *  and use Reston VA as the job location.
 */
object DefaultSettingsSetter {
  case class Command(sparkMaster: String = "", esNodes: String = "", esPort: String = "", esSettingsIndex: String = "")

  //logger
  val log: Logger = LoggerFactory.getLogger(getClass())

  def setDefaultSettings(options: Command) {
    //Create Spark configuration object, with Elasticsearch configuration
    val conf = new SparkConf().setMaster(options.sparkMaster)
      .setAppName("DefaultSettingsSetter").set("es.nodes", options.esNodes)
      .set("es.port", options.esPort)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    /*
      The internal hostname is ip-172-31-61-189.ec2.internal (172.31.61.189).  Internally the REST API is available on port 9200 and the native transport runs on port 9300.
    */

    //Create Spark RDD using conf
    val sc = new SparkContext(conf)

    val settings: RegressionSettings = RegressionSettings()

    log.info("Created default RegressionSettings")

    sc.parallelize(Seq(settings.toMap)).saveToEs(options.esSettingsIndex + "/settings", Map("es.mapping.id" -> "id"))

    log.info("RegressionSettings saved to Elasticsearch")
  }

  def main(args: Array[String]) {
    val parser = new OptionParser[Command]("DefaultSettingsSetter") {
      opt[String]('m', "master") required() valueName("<master>") action { (x, c) =>
        c.copy(sparkMaster = x)
      } text ("Spark master argument.")
      opt[String]('n', "nodes") required() valueName("<nodes>") action { (x, c) =>
        c.copy(esNodes = x)
      } text ("Elasticsearch node to connect to, usually IP address of ES server.")
      opt[String]('p', "port") required() valueName("<port>") action { (x, c) =>
          c.copy(esPort = x)
      } text ("Default HTTP/REST port used for connecting to Elasticsearch, usually 9200.")
      opt[String]('i', "settingsindex") required() valueName("<settingsindex>") action { (x, c) =>
          c.copy(esSettingsIndex = x)
      } text ("Name of the Elasticsearch index to save the settings to")

      note ("Uploads the default settings into the settings index of elasticsearch")
      help("help") text("Prints this usage text")
    }

    // Parses command line arguments and passes them to the search
    parser.parse(args, Command()) match {
        //If the command line options were all present continue
        case Some(options) =>
            //Read all of the files in sourceDirectory and use Tika to grab the text from each
            setDefaultSettings(options)
        //Elsewise, just exit
        case None =>
    }
  }
}
