package applicant.etl

import applicant.ml.rnn.TextNet

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

import scopt.OptionParser

import org.slf4j.{Logger, LoggerFactory}

/**
 * Generates resume(s) from attribute files.
 */
class ResumeGenerator(attributeDir: String, json: String, coefficients: String, saveDir: String) {
    // Attribute values
    private val firstNames = readValues(attributeDir + "/firstnames.txt")
    private val lastNames = readValues(attributeDir + "/lastnames.txt")
    private val titles = readValues(attributeDir + "/titles.txt")
    private val organizations = readValues(attributeDir + "/organizations.txt")
    private val locations = readValues(attributeDir + "/locations.txt")
    private val degrees = readValues(attributeDir + "/degrees.txt")
    private val schools = readValues(attributeDir + "/schools.txt")
    val direc = saveDir

    //logger
    val log: Logger = LoggerFactory.getLogger(getClass())

    // Random index generator
    private val generator = if (json != null) {
        new Random()
    }
    else {
        new Random(1024)
    }

    // Job text generator
    private val textNet: TextNet = if (json != null) {
        new TextNet(json, coefficients)
    }
    else {
        null
    }

    /**
     * Generates a resume as a sequence of Strings (lines).
     *
     * @return Seq[String]
     */
    def generate(): Seq[String] = {
        val lines = new ListBuffer[String]()

        // Generate name
        lines += getValue(firstNames) + " " + getValue(lastNames)
        lines += "\n"

        // Generate work experience
        lines += "WORK EXPERIENCE"
        for (x <- 0 until generator.nextInt(5) + 1) {
            val title = getValue(titles)

            lines += title
            lines += getValue(organizations) + " - " + getValue(locations)

            // Generate position text if a textnet is present
            if (textNet != null) {
                var text = textNet.getText(title.toUpperCase(), 300, 1)(0)
                lines += text.replaceAll("(?m)[A-Z\\s]{10,}\\s+?", "")
            }

            lines += "\n"
        }

        lines += "EDUCATION"
        lines += getValue(degrees)
        lines += getValue(schools)
        lines += "\n"

        return lines
    }

    /**
     * Gets a random value out of an Attribute sequence.
     *
     * @param values Seq[String] of all attribute values
     * @return String
     */
    private def getValue(values: Seq[String]): String = {
        return values(generator.nextInt(values.length))
    }

    /**
     * Reads all attributes in from a file. Each attribute is on a single line.
     *
     * @param file input file
     * @return Seq[String]
     */
    private def readValues(file: String): Seq[String] = {
        return Source.fromFile(file).getLines.toSeq
    }
}

object ResumeGenerator {
    // Command line arguments
    case class Command(attributeDir: String = "", json: String = "", coefficients: String = "", saveDir: String = "")

    //logger
    val log: Logger = LoggerFactory.getLogger(getClass())
    
    /**
     * Main method
     */
    def main(args: Array[String]) {

        var nameGet = false
        var name = ""
        var finalDir = ""
        val pdfGenerator = new PDFGenerator()

        val parser = new OptionParser[Command]("ResumeGenerator") {
            opt[String]('a', "attributeDir") required() valueName("<attributes dir>") action { (x, c) =>
                c.copy(attributeDir = x)
            } text("Attribute Directory")
            opt[String]('j', "json") required() valueName("<json>") action { (x, c) =>
                c.copy(json = x)
            } text("RNN Model JSON Configuration")
            opt[String]('c', "coefficients") required() valueName("<coefficients>") action { (x, c) =>
                c.copy(coefficients = x)
            } text("RNN Model Coefficients")
            opt[String]('d', "saveDir") required() valueName("<generated resumes pdf dir>") action { (x, c) =>
                c.copy(saveDir = x)
            } text("Generated Resumes Directory")

            note("Generates a Random Resume")
            help("help") text("Prints this usage text")
        }

        // Parses command line arguments and passes them to the search
        parser.parse(args, Command()) match {
            case Some(options) =>
                val generator = new ResumeGenerator(options.attributeDir, options.json, options.coefficients, options.saveDir)
                generator.generate().foreach { line =>
                    finalDir = generator.direc
                    if (line.endsWith("\n")) {
                        log.info(line)
                        val trimLine = line.replace('\n', ' ')
                        pdfGenerator.addLine(trimLine)
                    }
                    else {
                        log.info(line)
                        val trimLine2 = line.replace('\n', ' ')
                        pdfGenerator.addLine(trimLine2)

                        if (nameGet == false) {
                            name = trimLine2
                            nameGet = true
                        }
                    }
                }
            case None =>
        }

        pdfGenerator.end(name, finalDir)

    }
}
