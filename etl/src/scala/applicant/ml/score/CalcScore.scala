package applicant.ml.score

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import scopt.OptionParser
import scala.collection.mutable.HashMap
import applicant.nlp.LuceneTokenizer
import java.io.File

/**
 *
 */
object CalcScore {
  /**
   * Calculates first feature
   *
   * @param w2vmap Word2Vec synonym map
   * @param resume Full string of parsed resume
   * @return First feature score framed to 0-1
   */
  def firstFeature (w2vmap: HashMap[String,Boolean], resume: String): Double = {
    val tokenizer = new LuceneTokenizer()
    val resumeArray = tokenizer.tokenize(resume)
    var matches : Double = 0.0

    resumeArray.foreach { word =>
      if (w2vmap.contains(word)){
        w2vmap += (word -> true)
      }
    }

    w2vmap.foreach{ case (k,v) =>
      if (v == true){
        matches += 1
        w2vmap += (k -> false)
      }
    }

    val featuresScore = matches / 20
    return featuresScore
  }
}
