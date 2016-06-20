package applicant.ml.word2vec

import applicant.nlp.LuceneTokenizer

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import scopt.OptionParser

import java.io.File

/**
 * Builds and searches a Word2Vec structure for given terms
 */
object WordMap {
    // Command line arguments
    case class Command(term: String = "", data: String = "", model: String = "")

    /**
     * Searches and builds a Word2Vec structure.
     *
     * @param options command line options
     */
    def search(options: Command) {
      val conf = new SparkConf()

      // Default parameters if not passed from command line
      if (conf.get("spark.master", null) == null) {
        conf.setAppName("WordMap")
        conf.setMaster("local[*]")
      }

      val sc = new SparkContext(conf)
      val input = sc.textFile(options.data).map(line => new LuceneTokenizer().tokenize(line))

      if (!new File(options.model).exists()) {
        val model = new Word2Vec().fit(input)
        model.save(sc, options.model)
      }

      // Load model
      val model = Word2VecModel.load(sc, options.model)

      val synonyms = model.findSynonyms(options.term.toLowerCase(), 25)
      for((synonym, cosineSimilarity) <- synonyms) {
        println(s"$synonym $cosineSimilarity")
      }

      sc.stop()
    }

    /**
     * Main method
     */
    def main(args: Array[String]) {
      val parser = new OptionParser[Command]("WordMap") {
        opt[String]('t', "term") required() valueName("<term>") action { (x, c) =>
          c.copy(term = x)
        } text("Search Term")
        opt[String]('d', "data") required() valueName("<data>") action { (x, c) =>
          c.copy(data = x)
        } text("Path to input data")
        opt[String]('m', "model") required() valueName("<model>") action { (x, c) =>
          c.copy(model = x)
        } text("Path to model data")

        note("Searches a word2vec structure for similar terms.\n")
        help("help") text("Prints this usage text")
      }

      // Parses command line arguments and passes them to the search
      parser.parse(args, Command()) match {
        case Some(options) =>
          search(options)
        case None =>
      }
    }
}
