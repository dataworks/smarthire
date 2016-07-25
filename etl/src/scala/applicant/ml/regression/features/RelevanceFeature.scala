package applicant.ml.regression.features

import applicant.etl.ApplicantData
import applicant.nlp.LuceneTokenizer
import applicant.ml.regression.FeatureSetting
import applicant.ml.naivebayes.{NaiveBayesFeatureGenerator, NaiveBayesHelper}

import scala.collection.mutable.ListBuffer
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.IDFModel

class RelevanceFeature(newSetting: FeatureSetting, naiveBayesModel: NaiveBayesModel, tfIdfModel: IDFModel) extends BaseFeature {
  val setting: FeatureSetting = newSetting
  val bayesModel: NaiveBayesModel = naiveBayesModel
  val idfModel: IDFModel = tfIdfModel

  /**
   *  Will return a score of the word frequency relevance
   *
   * @param applicant The applicant whose feature is checked
   */
  def getFeatureScore(applicant: ApplicantData): Double = {
    val tokenList = LuceneTokenizer.getTokens(applicant.fullText)
    var scores = new ListBuffer[Double]()

    tokenList.foreach { tokens =>
      val score = NaiveBayesHelper.predictSingleScore(bayesModel, NaiveBayesFeatureGenerator.getAdjustedFeatureVec(tokens, idfModel))
      scores += score
    }

    // Filter overconfident scores. Model confidence with vary more with larger training sets.
    scores = scores.filter { score =>
      score < 0.95 && score > 0.05
    }

    var result = 0.0
    if (scores.length > 0) {
      result = scores.sum / scores.length
    }

    return result
  }
}
