package applicant.ml.score

import scala.collection.mutable.ListBuffer
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import scopt.OptionParser
import org.elasticsearch.spark._
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.mllib.linalg.{Vectors, Vector}

import applicant.ml.regression._
import applicant.etl._

/**
 * ScoreCalculator is a class that loads a logistic regression model, queries elasticsearch
 *  for applicants, and then updates their scores based on the model
 */
object ScoreCalculator {
  case class Command(word2vecModel: String = "", sparkMaster: String = "",
    esNodes: String = "", esPort: String = "", esAppIndex: String = "",
    regressionModelDirectory: String = "")

  def reloadScores(options: Command) {
    val conf = new SparkConf().setMaster(options.sparkMaster)
      .setAppName("generateMLmodel").set("es.nodes", options.esNodes)
      .set("es.port", options.esPort)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    //create spark rdd using conf
    val sc = new SparkContext(conf)

    //Load the w2v and logistic regression models
    val w2vModel = Word2VecModel.load(sc, options.word2vecModel)
    var regressionModel = LogisticRegressionHelper.loadModel(sc, options.regressionModelDirectory) match {
      case Some(model) =>
        model
      case None =>
        null
    }

    if (regressionModel == null) {
      println("There was a problem loading the regression model. Quitting now.")
      return
    }

    //Query elasticsearch for every applicant
    val appRDD = sc.esRDD(options.esAppIndex + "/applicant").values

    //accumulator
    val counter = sc.accumulator(0)

    //Create applicant data objects out of what was queried and find scores for each
    val appDataArray = appRDD.map { appMap =>
      val app = ApplicantData(appMap)
      val features = FeatureGenerator.getLogisticFeatureVec(w2vModel, app)
      val calculatedScore = LogisticRegressionHelper.predictSingleScore(regressionModel, features)
      app.score = Math.round(calculatedScore * 100.0) / 100.0

      println("Scoring applicant number " + counter + " with id of " + app.applicantid + ". Score = " + app.score)
      counter += 1

      app.toMap
    }.saveToEs(options.esAppIndex + "/applicant", Map("es.mapping.id" -> "id"))
  }


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
      opt[String]('d', "regressionmodeldirectory") required() valueName("<regressionmodeldirectory>") action { (x, c) =>
        c.copy(regressionModelDirectory = x)
      } text ("The path to the logistic regression model")

      note ("Pulls labeled resumes from elasticsearch and generates a logistic regression model \n")
      help("help") text("Prints this usage text")
    }

    // Parses command line arguments and passes them to the search
    parser.parse(args, Command()) match {
        //If the command line options were all present continue
        case Some(options) =>
          //Read all of the files in sourceDirectory and use Tika to grab the text from each
          reloadScores(options)
        //Elsewise, just exit
        case None =>
    }
  }
}
