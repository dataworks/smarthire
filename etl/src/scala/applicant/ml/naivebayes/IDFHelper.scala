package applicant.ml.naivebayes

import applicant.ml.ModelUtils

import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.feature.{IDF, IDFModel, HashingTF}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils
import org.apache.commons.io.FileUtils
import java.nio.file.{Files, Paths}
import java.io.File

object IDFHelper {
  /**
   * Creates a model out of the given RDD of term frequency vectors
   *
   * @param data An RDD of term frequency vectors, used to calculate the Inverse Document Frequency values
   * @return The created model
   */
  def createModel(data: RDD[Vector]): IDFModel = {
    return new IDF(minDocFreq = 2).fit(data)
  }

  /*
  * A helper to store shared saving code
  */
  private def saveModelHelper(model: IDFModel, saveLoc: String) {
    val modelPath = if (saveLoc.endsWith("/")) (saveLoc + "IDF.model") else (saveLoc + "/IDF.model")

    //Clear out the path in case there is already a model at saveLoc
    ModelUtils.clearCustomModelPath(modelPath)

    val modelBytes = ModelUtils.serialize(model)

    //Save the model to the specified location
    FileUtils.writeByteArrayToFile(new File(modelPath), modelBytes)
  }

  /**
   * Creates a model out of the given frequency vectors and saves it to a file
   * Will overwrite any model already at saveLoc
   *
   * @param data An RDD of term frequency vectors, used to calculate the Inverse Document Frequency values
   * @param saveLoc The directory location where model will be saved.
   *                  The filename of IDF.model will be appended
   */
  def createAndSaveModel(data: RDD[Vector], saveLoc: String) {
    val model = createModel(data)
    saveModelHelper(model, saveLoc)
  }

  /**
   * Saves an IDFModel to a specified location
   * Will overwrite any model already at saveLoc
   *
   * @param model The model that needs to be saved
   * @param saveLoc The location where model will be saved
   */
  def saveModel(model: IDFModel, saveLoc: String) {
    saveModelHelper(model, saveLoc)
  }

  /**
   * Loads an IDF model that was previously saved
   *
   * @param loadLoc The location where the the model was saved to
   * @return The NaiveBayesModel loaded from the loadLoc
   */
  def loadModel(loadLoc: String): Option[IDFModel] = {
    //Load the bytes from the file
    val loadPath = Paths.get(if (loadLoc.endsWith("/")) (loadLoc + "IDF.model") else (loadLoc + "/IDF.model"))
    if (Files.exists(loadPath)) {
      val byteArray = Files.readAllBytes(loadPath)

      //deserialize the byte array as its original model
      val loadedModel: IDFModel = ModelUtils.deserialize(byteArray)
      return Some(loadedModel)
    }
    else {
      return None
    }
  }

  /**
   * Will give a prediction for a single set of features.
   *
   * @param model The model used to test the features against
   * @param features A single list of features
   * @return A vector of adjusted term frequencies
   */
  def adjustTFVector(model: IDFModel, tfvec: Vector): Vector = {
    return model.transform(tfvec)
  }
}
