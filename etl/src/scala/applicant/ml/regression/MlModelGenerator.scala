package applicant.ml.regression

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.elasticsearch.spark._
import scopt.OptionParser
import scala.collection.mutable.HashMap
import applicant.nlp.LuceneTokenizer
import applicant.etl._
import java.io.File
import java.lang._
import java.util.regex
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import scala.collection.mutable.{ListBuffer, Map}

/**
 *
 */
object MlModelGenerator {

  //Class to store command line options
  case class Command(word2vecModel: String = "", sparkMaster: String = "",
    esNodes: String = "", esPort: String = "", esAppIndex: String = "",
    esLabelIndex: String ="")

  def generateMLmodel(options: Command) {
    val archivedAppListBuff = scala.collection.mutable.ListBuffer.empty[ApplicantData]
    val favoritedAppListBuff = scala.collection.mutable.ListBuffer.empty[ApplicantData]

    val conf = new SparkConf().setMaster(options.sparkMaster)
      .setAppName("generateMLmodel").set("es.nodes", options.esNodes)
      .set("es.port", options.esPort)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    //Create Spark RDD using conf
    val sc = new SparkContext(conf)
    //Create Word2Vec model
    val w2vModel = Word2VecModel.load(sc, options.word2vecModel)
    val archiveLabelsSeq = sc.esRDD(options.esLabelIndex + "/label", "?q=type:archive").collectAsMap()
    archiveLabelsSeq.foreach{ label =>
      val labelMap = label._2
      val applicantid = labelMap("id")
      val archivedAppRDD = sc.esRDD(options.esAppIndex + "/applicant", "?q=id:" + applicantid).collectAsMap()
      val archivedAppMap = archivedAppRDD(applicantid.toString())
      val archivedAppMutableMap = collection.mutable.Map(archivedAppMap.toSeq: _*)
      println(ApplicantData(archivedAppMutableMap))
      //archivedAppListBuff += ApplicantData(archivedAppMap)
      //println((archivedAppMap))
      //println(ApplicantData(archivedAppMap))*/
    }

  }

  /**
   * Main method
   *
   * @param args Array of Strings: see options
   */
  def main(args: Array[String]) {

    //Command line option parser
    val parser = new OptionParser[Command]("ResumeParser") {
      opt[String]('w', "word2vecModel") required() valueName("<word2vecModel>") action { (x, c) =>
        c.copy(word2vecModel = x)
      } text ("Path to word2vec model (usually model/w2v)")
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
      } text ("Name of the Elasticsearch index to read and write data.")
      opt[String]('l', "labelindex") required() valueName("<labelindex>") action { (x, c) =>
        c.copy(esLabelIndex = x)
      } text ("Name of the Elasticsearch containing archived/favorited labes.")

      note ("Pulls labeled resumes from elasticsearch and generates a logistic regression model \n")
      help("help") text("Prints this usage text")
    }

    // Parses command line arguments and passes them to the search
    parser.parse(args, Command()) match {
        //If the command line options were all present continue
        case Some(options) =>
          //Read all of the files in sourceDirectory and use Tika to grab the text from each
          generateMLmodel(options)
        //Elsewise, just exit
        case None =>
    }
  }
}
