package applicant.ml.regression

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, LogisticRegressionModel}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.MustMatchers._

import org.apache.commons.io.FileUtils
import java.io.File

class LogisticRegressionHelperSpec extends FlatSpec with BeforeAndAfterAll {
  /**
   * Scala Test Spec to test the LogisticRegressionHelper object.
   */

  val conf = new SparkConf().setMaster("local[*]").setAppName("ResumeReaderSpec")
  val sc = new SparkContext(conf)
  val simpleDataset: List[LabeledPoint] =
    List (
      LabeledPoint(1.0, Vectors.dense(1.0)),
      LabeledPoint(0.0, Vectors.dense(0.0))
    ) //data has never been as statistically significant as this

   override def afterAll() = {
     sc.stop()
   }

  "createModel" must "properly create a model" in {
    val model = LogisticRegressionHelper.createModel(sc, simpleDataset)
    model mustBe a [LogisticRegressionModel]
  }

  "createAndSaveModel" must "create and write a model" in {
    LogisticRegressionHelper.createAndSaveModel(sc, "data/test/regression", simpleDataset)

    //Check if the data and metadata folder exists
    val dataTest: File = new File ("data/test/regression/data/_SUCCESS")
    dataTest.exists() mustBe (true)

    var metadataTest: File = new File("data/test/regression/metadata/_SUCCESS")
    metadataTest.exists() mustBe (true)
  }

  "loadModel" must "load a LogisticRegressionModel" in {
    LogisticRegressionHelper.loadModel(sc, "data/test/regression") mustBe a [Some[_]]
  }

  "loadModel" must "return None when model path is incorrect" in {
    LogisticRegressionHelper.loadModel(sc, "foo/bar") mustBe None
  }

  "predictSingleScore" must "accurately predict data" in {
    var model: LogisticRegressionModel = null
    LogisticRegressionHelper.loadModel(sc, "data/test/regression") match {
        case Some(regressionModel) =>
          model = regressionModel
        case None =>
          model = null
    }

    model must not be null

    var testFeature: Double = 0.0

    while (testFeature <= 1.0) {
      println("The value at " + testFeature + " is " + LogisticRegressionHelper.predictSingleScore(model, Vectors.dense(testFeature)))
      testFeature += 0.1
    }

    LogisticRegressionHelper.predictSingleScore(model, Vectors.dense(0.0)) mustBe 0.0
    LogisticRegressionHelper.predictSingleScore(model, Vectors.dense(1.0)) mustBe 1.0
    //LogisticRegressionHelper.predictSingleScore(model, Vectors.dense(0.5)) mustBe 0.5
  }
}
