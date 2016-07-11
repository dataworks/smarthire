package applicant.ml.naivebayes

import applicant.nlp.LuceneTokenizer
import applicant.etl.ApplicantData
import org.apache.spark.mllib.feature.{HashingTF, IDFModel}
import org.apache.spark.mllib.linalg.{Vectors, Vector, SparseVector}

object NaiveBayesFeatureGenerator {
  val htf = new HashingTF(10000)
  val tokenizer = new LuceneTokenizer("english")

  /**
   * Will return a vector of bucket size counts for the tokens from
   *  the full body of the resume
   *
   * @param applicant The data for the current applicant
   * @return A Vector of feature counts
   */
  def getFeatureVec(tokens: Seq[String]): Vector = {
    return htf.transform(tokens)
  }

  /**
   * Will return a vector of bucket size counts for the tokens from
   *  the full body of the resume that are adjusted by an IDFModel
   *
   * @param applicant The data for the current applicant
   * @return A Vector of feature counts
   */
  def getAdjustedFeatureVec(tokens: Seq[String], model: IDFModel): Vector = {
    return model.transform(getFeatureVec(tokens))
  }
}
