package applicant.etl

import java.io.{File, FileWriter}

import scopt.OptionParser

/**
 * Merges a directory of downloaded resumes with generated resumes into a single file used for training.
 */
object MergeResumes {
    // Command line arguments
    case class Command(outputFile: String = "", format: String = "", resumeDir: String = "", attributeDir: String = "", count: Int = 0)

    /**
     * Tags, formats and writes all resumes in resumeDir into writer.
     *
     * @param resumeDir directory of text based resumes
     * @param writer output writer
     */
    def writeResumes(resumeDir: String, writer: ResumeWriter) {
        val files = new File(resumeDir).listFiles.filter(_.getName().endsWith(".txt")).toSeq
        for (file <- files) {
            // Parse input file and write to output file
            writer.write(file)
        }
    }

    /**
     * Generates count number of resumes from attributes stored in attributeDir.
     *
     * @param attributeDir directory of attribute information
     * @param count number of resumes to generate
     * @param writer output writer
     */
    def generateResumes(attributeDir: String, count: Int, writer: ResumeWriter) {
        val generator = new ResumeGenerator(attributeDir, null, null, null)
        for (x <- 0 until count) {
            writer.write(generator.generate())
        }
    }

    /**
     * Builds training data using input command line options.
     *
     * @param options command line options
     */
    def buildData(options: Command) {
        val output = new FileWriter(options.outputFile)

        try {
            val writer = new ResumeWriter(output, options.format)

            // Write and tag stored resumes
            writeResumes(options.resumeDir, writer)

            if (options.format == "nlp") {
                // Generate and tag resumes based on attribute data
                generateResumes(options.attributeDir, options.count, writer)
            }
        }
        finally {
            output.close()
        }
    }

    /**
     * Main method
     */
    def main(args: Array[String]) {
        val parser = new OptionParser[Command]("MergeResumes") {
            opt[String]('o', "outputFile") required() valueName("<output file>") action { (x, c) =>
                c.copy(outputFile = x)
            } text("Output File")
            opt[String]('f', "format") required() valueName("<format>") action { (x, c) =>
                c.copy(format = x)
            } text("Output Format (nlp|rnn)")
            opt[String]('r', "resumeDir") required() valueName("<resume dir>") action { (x, c) =>
                c.copy(resumeDir = x)
            } text("Resume Directory")
            opt[String]('a', "attributeDir") valueName("<attributes dir>") action { (x, c) =>
                c.copy(attributeDir = x)
            } text("Attribute Directory")
            opt[Int]('c', "generated count") valueName("<generated count>") action { (x, c) =>
                c.copy(count = x)
            } text("Number of resumes to generate")

            note("Merges and tags resumes into a single file for training.\n")
            help("help") text("Prints this usage text")
        }

        // Parses command line arguments and passes them to the search
        parser.parse(args, Command()) match {
            case Some(options) =>
                buildData(options)
            case None =>
        }
    }
}
