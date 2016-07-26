package applicant.ml.naivebayes

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.mllib.feature.{IDF, IDFModel, HashingTF}
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._

import applicant.nlp.LuceneTokenizer
import applicant.etl.ApplicantData

import org.apache.commons.io.FileUtils
import java.io.File

class IDFHelperSpec extends FlatSpec {
  /**
   * Scala Test Spec to test the IDFHelper object.
   */

  val conf = new SparkConf().setMaster("local[1]").setAppName("IDFHelperSpec")
  val sc = new SparkContext(conf)

  val htf = new HashingTF(10000)
  val tokenizer = new LuceneTokenizer()

  val cristo1 = FileUtils.readFileToString(new File("data/test/idf/cristo1.txt"))
  val cristo1Vec = htf.transform(tokenizer.tokenize(cristo1))

  val cristo2 = FileUtils.readFileToString(new File("data/test/idf/cristo2.txt"))
  val cristo2Vec = htf.transform(tokenizer.tokenize(cristo2))

  val testRDD = sc.parallelize(Array(cristo1Vec, cristo2Vec))

  "createModel" must "properly create a model" in {
    val model = IDFHelper.createModel(testRDD)
    model mustBe an [IDFModel]
  }

  "createAndSaveModel" must "properly save a model" in {
    IDFHelper.createAndSaveModel(testRDD, "data/test/idf/")

    val checkFile = new File("data/test/idf/IDF.model")
    checkFile.exists() mustBe (true)
  }

  "loadModel" must "reload a useable model" in {
    val model = IDFHelper.loadModel("data/test/idf/")
    model mustBe an [Option[_]]
  }

  "IDFHelper" must "not change the internal representation" in {
    val model = IDFHelper.createModel(testRDD)
    val firstVec = model.idf

    IDFHelper.saveModel(model, "data/test/idf/")

    val sameModel = IDFHelper.loadModel("data/test/idf/").get

    val secondVec = sameModel.idf

    firstVec must equal (secondVec)
  }
}
