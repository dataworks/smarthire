package applicant.ml.score

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import scopt.OptionParser

import java.io.File

/**
 * Builds and searches a Word2Vec structure for given terms
 */
object CalcScore {
    // Command line arguments
    case class Command(term: String = "", data: String = "", model: String = "")

    /**
     * Calculates score
     *
     * @param options command line options
     */
    def calculate(options: Command) {

    }

    /**
     * Main method
     */
    def main(args: Array[String]) {
        val parser = new OptionParser[Command]("CalcScore") {
            opt[String]('t', "term") required() valueName("<term>") action { (x, c) =>
                c.copy(term = x)
            } text("Search Term")
            opt[String]('d', "data") required() valueName("<data>") action { (x, c) =>
                c.copy(data = x)
            } text("Path to input data")
            opt[String]('m', "model") required() valueName("<model>") action { (x, c) =>
                c.copy(model = x)
            } text("Path to model data")

            note("Calculates an applicant score based on several features.\n")
            help("help") text("Prints this usage text")
        }

        // Parses command line arguments and passes them to the search
        parser.parse(args, Command()) match {
            case Some(options) =>
                calculate(options)
            case None =>
        }
    }
}
