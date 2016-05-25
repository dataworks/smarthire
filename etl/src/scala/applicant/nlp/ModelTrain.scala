package applicant.nlp

import java.io.{File, FileInputStream, FileWriter}

import scopt.OptionParser

/**
 * Trains an OpenNLP model.
 */
object ModelTrain {
    // Command line arguments
    case class Command(inputFile: String = "", outputFile: String = "", entity: String = "", cutoff: Int = 5)

    /**
     * Trains a NLP model using command line options.
     *
     * @param options command line options
     */
    def train(options: Command) {
        var inputStream: FileInputStream = null

        try {
            inputStream = new FileInputStream(options.inputFile)

            // Train and write the model
            val model = new EntityModel()
            model.train(inputStream, options.entity, options.cutoff)
            model.write(new File(options.outputFile))
        }
        finally {
            if (inputStream != null) {
                inputStream.close()
            }
        }
    }

    /**
     * Main method
     */
    def main(args: Array[String]) {
        val parser = new OptionParser[Command]("ModelTrain") {
            opt[String]('i', "inputFile") required() valueName("<input file>") action { (x, c) =>
                c.copy(inputFile = x)
            } text("Training input file")
            opt[String]('o', "outputFile") required() valueName("<output file>") action { (x, c) =>
                c.copy(outputFile = x)
            } text("Model output file")
            opt[String]('e', "entity") required() valueName("<entity type>") action { (x, c) =>
                c.copy(entity = x)
            } text("Entity Type")
            opt[Int]('c', "cutoff") valueName("<cutoff>") action { (x, c) =>
                c.copy(cutoff = x)
            } text("Cutoff parameter to pass to OpenNLP")

            note("Builds OpenNLP Model\n")
            help("help") text("Prints this usage text")
        }

        // Parses command line arguments and passes them to the search
        parser.parse(args, Command()) match {
            case Some(options) =>
                train(options)
            case None =>
        }
    }
}
