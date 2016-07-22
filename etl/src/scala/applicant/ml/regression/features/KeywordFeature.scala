package applicant.ml.regression.features

import applicant.etl.ApplicantData
import applicant.nlp.LuceneTokenizer
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

class KeywordFeature(newName: String) extends BaseFeature {
  val name = newName

  /**
   *  Will return a score of the number of keywords from values that applear
   *    at least twice in the applican's resume
   *
   * @param applicant The applicant whose feature is checked
   * @param values Any configurable options for the feature
   */
  def getFeatureScore(applicant: ApplicantData, values: ListBuffer[AnyRef]): Double = {
    val keywordList: ListBuffer[String] = values.asInstanceOf[ListBuffer[String]]

    val tokenizer = new LuceneTokenizer()
    val resumeArray = tokenizer.tokenize(applicant.fullText) //Converts to lowercase
    var matches : Double = 0.0

    val map = HashMap.empty[String, Int]

    for (item <- keywordList) {
      map += (item.toLowerCase -> 0)
    }

    resumeArray.foreach { word =>
      if (map.contains(word)){
        val currentWordCount = map(word)
        map += (word -> (currentWordCount + 1))
      }
    }

    map.foreach{ case (k,v) =>
      if (v >= 2){
        if (v > 5) {
          matches += 5.0
        }
        else {
          matches += v.toDouble
        }
      }
    }

    val rawScore = matches/(map.size*4.0)

    return if (rawScore > 1.0) 1.0 else rawScore
  }
}
