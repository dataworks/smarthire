package applicant.ml.naivebayes

import applicant.nlp.LuceneTokenizer
import applicant.etl.ApplicantData
import org.apache.spark.mllib.feature.{HashingTF, IDFModel}
import org.apache.spark.mllib.linalg.{Vectors, Vector}

object NaiveBayesFeatureGenerator {
  val htf = new HashingTF(10000)
  val tokenizer = new LuceneTokenizer()

  /**
   * Will return a vector of bucket size counts for the tokens from
   *  the full body of the resume
   *
   * @param applicant The data for the current applicant
   * @return A Vector of feature counts
   */
  def getFeatureVec(applicant: ApplicantData): Vector = {
    return htf.transform(tokenizer.tokenize(applicant.fullText))
  }

  /**
   * Will return a vector of bucket size counts for the tokens from
   *  the full body of the resume that are adjusted by an IDFModel
   *
   * @param applicant The data for the current applicant
   * @return A Vector of feature counts
   */
  def getAdjustedFeatureVec(applicant: ApplicantData, model: IDFModel): Vector = {
    //println("The transformed score is " + model.transform(getFeatureVec(applicant)))
    return model.transform(getFeatureVec(applicant))
  }
}
