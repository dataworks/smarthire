package applicant.ml.rnn

import java.io.{BufferedReader, InputStreamReader}

import scopt.OptionParser

/**
 * Tests a TextNet model.
 */
object TextNetTest {
    // Command line arguments
    case class Command(json: String = "", coefficients: String = "")

    /**
     * Builds a TextNet network and generates text samples from it.
     *
     * @param options command line options
     */
    def execute(options: Command) {
        val generator = new TextNet(options.json, options.coefficients)
        val reader = new BufferedReader(new InputStreamReader(System.in))

        while (true) {
            val line = reader.readLine()
            println()

            val results = generator.getText(line, 300, 1)
            results.foreach { result =>
                println(result)
                println()
            }
        }
    }

    /**
     * Main method
     */
    def main(args: Array[String]) {
        val parser = new OptionParser[Command]("TextNetTest") {
            opt[String]('j', "json") required() valueName("<json>") action { (x, c) =>
                c.copy(json = x)
            } text("RNN Model JSON Configuration")
            opt[String]('c', "coefficients") required() valueName("<coefficients>") action { (x, c) =>
                c.copy(coefficients = x)
            } text("RNN Model Coefficients")

            note("Generates input text from a given Char RNN model")
            help("help") text("Prints this usage text")
        }

        // Parses command line arguments and passes them to the search
        parser.parse(args, Command()) match {
            case Some(options) =>
                execute(options)
            case None =>
        }
    }
}
