package applicant.ml.naivebayes

import applicant.nlp.LuceneTokenizer
import applicant.etl.ApplicantData
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.{Vectors, Vector}

object NaiveBayesFeatureGenerator {
  val htf = new HashingTF(40)
  val tokenizer = new LuceneTokenizer()

  /**
   * Will return a vector of bucket size counds for the tokens from
   *  the full body of the resume
   *
   * @param applicant The data for the current applicant
   * @return A Vector of feature counts
   */
  def getFeatureVec(applicant: ApplicantData): Vector = {
    //println(htf.transform(tokenizer.tokenize(applicant.fullText)))//TODO: delete this
    return htf.transform(tokenizer.tokenize(applicant.fullText))
  }
}
