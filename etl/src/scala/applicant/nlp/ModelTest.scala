package applicant.nlp

import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern

import opennlp.tools.namefind.{NameFinderME, NameSample, RegexNameFinder, TokenNameFinder, TokenNameFinderModel}
import opennlp.tools.tokenize.WhitespaceTokenizer
import opennlp.tools.util.{ObjectStream, PlainTextByLineStream, Span}

import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer, Map}
import scala.io.Source

import scopt.OptionParser

/**
 * Tests entity models against input from stdin.
 */
object ModelTest {
    // Command line arguments
    case class Command(models: String = "", patterns: String = "")

    /**
     * Reads a list of entities to find based on regular expressions.
     *
     * @param file input file listing regular expressions
     * @return RegexNameFinder
     */
    def getRegexNameFinder(file: String) : RegexNameFinder = {
        val patterns: Map[String, Array[Pattern]] = Map()

        val lines = Source.fromFile(file).getLines.toSeq
        for (line <- lines) {
            if (line.trim().length() > 0 && !line.startsWith("#")) {
                println(line)

                // Add regex pattern/type mapping
                val expression: Array[String] = line.split("\\s*=\\s*")
                patterns(expression(0)) = Array(Pattern.compile(expression(1)))
            }
        }

        return new RegexNameFinder(patterns)
    }

    /**
     * Executes the model test using command line options passed in.
     *
     * @param options command line options
     */
    def execute(options: Command) {
        val models = options.models.split(",")

        // Load trained models to tag
        var nameFinders = new Array[TokenNameFinder](models.length)
        for (x <- 0 until nameFinders.length) {
            println("Loading model - " + models(x))

            val model = new TokenNameFinderModel(new File(models(x)))
            nameFinders(x) = new NameFinderME(model)
        }

        // Load regex patterns to tag
        if (options.patterns != null) {
            println("Loading patterns - " + options.patterns)
            nameFinders = nameFinders :+ getRegexNameFinder(options.patterns)
        }

        println("Ready for input")

        var line: String = null
        val untokenizedLineStream = new PlainTextByLineStream(System.in, Charset.forName("UTF-8"))
        while ({ line = untokenizedLineStream.read(); line != null }) {
            val whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE.tokenize(line)
            if (whitespaceTokenizerLine.length == 0) {
                for (nameFinder <- nameFinders) {
                    nameFinder.clearAdaptiveData()
                }
            }

            val names: ListBuffer[Span] = new ListBuffer[Span]()
            for (nameFinder <- nameFinders) {
                names.appendAll(nameFinder.find(whitespaceTokenizerLine))
            }

            val reducedNames = NameFinderME.dropOverlappingSpans(names.toArray)
            val nameSample = new NameSample(whitespaceTokenizerLine, reducedNames, false)

            printEntities(nameSample)
        }
    }

    /**
     * Prints the entities tagged by the nlp models.
     *
     * @param sample tagged entity
     */
    def printEntities(sample: NameSample) {
        val sentence = sample.getSentence()
        val names = sample.getNames()

        for (name <- names) {
            // Build and clean entity
            var entity = sentence.slice(name.getStart(), name.getEnd()).mkString(" ")
            entity = entity.replaceAll("\\,$", "")

            println(name.getType() + "->" + entity)
        }
    }

    /**
     * Main method
     */
    def main(args: Array[String]) {
        val parser = new OptionParser[Command]("ModelTest") {
            opt[String]('m', "models") required() valueName("<model1,model2,model3>") action { (x, c) =>
                c.copy(models = x)
            } text("NLP Model List")
            opt[String]('p', "patterns") valueName("<pattern file>") action { (x, c) =>
                c.copy(patterns = x)
            } text("File containing RegEx patterns to tag")

            note("Tests OpenNLP Models\n")
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
