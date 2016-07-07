package applicant.ml.naivebayes

import applicant.ml.ModelUtils

import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.classification.{NaiveBayesModel, NaiveBayes}
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils
import java.io.File

object NaiveBayesHelper {
  /**
   * Creates a model out of the given LabeledPoints and returns it
   *
   * @param sc A spark context; given to allow distributed processing
   * @param dataPoints A sequence of LabeledPoint objects used to train the model
   * @return The created model
   */
  def createModel(sc: SparkContext, dataPoints: Seq[LabeledPoint]): NaiveBayesModel = {
    //create an RDD out of the data points
    val pointRdd = sc.parallelize(dataPoints)

    //create the NaiveBayesModel from the NaiveBayes object
    val result = NaiveBayes.train(pointRdd, 1.0, modelType = "multinomial")
    return result
  }

  /**
   * Creates a model out of the given LabeledPoints and saves it to a file
   * Will overwrite any model already at saveLoc
   *
   * @param sc A spark context; given to allow distributed processing
   * @param saveLoc The location that model will be saved
                     => human-readable (JSON) model metadata is saved to saveLoc/metadata/
                     => Parquet formatted data is saved to saveLoc/data/
   * @param dataPoints A sequence of LabeledPoint objects used to train the model
   */
  def createAndSaveModel(sc: SparkContext, saveLoc: String, dataPoints: Seq[LabeledPoint]) {
    //clear out the path location in case a model is already saved there
    ModelUtils.clearModelPath(saveLoc)
    //save the model to the specified location
    createModel(sc, dataPoints).save(sc, saveLoc)
  }

  /**
   * Saves a NaiveBayesModel to a specified location
   * Will overwrite any model already at saveLoc
   *
   * @param model The model that needs to be saved
   * @param sc A spark context
   * @param saveLoc The location that model will be saved
                     => human-readable (JSON) model metadata is saved to saveLoc/metadata/
                     => Parquet formatted data is saved to saveLoc/data/
   */
  def saveModel(model: NaiveBayesModel, sc: SparkContext, saveLoc: String) {
    //clear out the path location in case a model is already saved there
    ModelUtils.clearModelPath(saveLoc)
    //save to model to saveLoc
    model.save(sc, saveLoc)
  }

  /**
   * Loads a NaiveBayes model that was previously saved
   *
   * @param sc A spark context; used during loading
   * @param loadLoc The location where the the model was saved to
   * @return The NaiveBayesModel loaded from the loadLoc
   */
  def loadModel(sc: SparkContext, loadLoc: String): Option[NaiveBayesModel] = {
    val testDataPath: File = new File(loadLoc + "/data")
    val testMetadataPath: File = new File(loadLoc + "/metadata")

    if (testDataPath.exists() && testMetadataPath.exists()) {
      return Some(NaiveBayesModel.load(sc, loadLoc))
    }

    return None
  }


  /**
   * Will give predictions from a feature set using the given model.
   * This method is especially useful for test as it returns pairs of
   *  predicted results and actual results.
   *
   * @param model The model used to test the features against
   * @param dataPoints An RDD of LabeledPoints
   * @return A list of Double pairs. The first Double is the prediction and the second is the actual given label
   */
  def predictLabeledScores(model: NaiveBayesModel, dataPoints: RDD[LabeledPoint]): RDD[(Double, Double)] = {
    return dataPoints.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }
  }

  /**
   * Will give predictions from a feature set using the given model.
   * This method is used for predicting data that has not been given
   *  a label yet.
   *
   * @param model The model used to test the features against
   * @param vectors An RDD of Vectors that hold the values of the features
   * @return A list of predictions from each of the Vectors
   */
  def predictUnlabeledScores(model: NaiveBayesModel, vectors: RDD[Vector]): RDD[Double] = {
    return vectors.map { features =>
      model.predict(features)
    }
  }

  /**
   * Will give a prediction for a single set of features.
   *
   * @param model The model used to test the features against
   * @param features A single list of features
   * @return A prediction for the given Vector of features
   */
  def predictSingleScore(model: NaiveBayesModel, feature: Vector): Double = {
    val prediction = model.predictProbabilities(feature).toArray
    val result = if (model.labels(0) == 1.0) prediction(0) else prediction(1)
    println("Adjusted score is " + result)
    return result
  }
}
