package applicant.ml.score

import applicant.nlp.LuceneTokenizer

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}

import java.io.File

/**
 * Modification of the word2vec class for scoring resumes based on search terms
 */
object W2VFeature {
  /**
   * Given a search term and a data source, returns the average cosine
   * similarity of the top 5 synonyms in the data
   *
   * @param sc Pre-configured SparkContext
   * @param term The search term to compare the data against
   * @param data The parsed full text of the resume to be used
   * @param modelPath
   * @return A double value representing the average score of the top 5 synonyms
   *
   */
    def feature(sc: SparkContext, term: String, data: String, modelPath: String) : Double = {
        val dataLines : Array[String] = data.split(System.getProperty("line.seperator"));
        val input = sc.parallelize(dataLines).map(line => new LuceneTokenizer().tokenize(line))

        if (!new File(modelPath).exists()) {
            val w2vModel = new Word2Vec().fit(input)
            w2vModel.save(sc, modelPath)
        }

        // Load model
        val w2vModel = Word2VecModel.load(sc, modelPath)

        val synonyms = w2vModel.findSynonyms(term.toLowerCase(), 10)
        var totalScore = 0.0
        for((synonym, cosineSimilarity) <- synonyms) {
            totalScore += cosineSimilarity
        }

        val averageScore = totalScore / synonyms.length

        return averageScore
    }

}
