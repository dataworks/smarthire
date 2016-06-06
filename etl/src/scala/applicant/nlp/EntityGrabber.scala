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
 * Uses entity models to parse information from a resume text string
 * @param modelFiles comma separated paths to NLP model binaryFiles
 * @param patterns a path to a file containing regular expression files
 */
class EntityGrabber(modelFiles: String, patterns: String) {
    //Initialization
    //grab the names out of the modelFiles string
    val models = modelFiles.split(",")

    // Load trained models to tag
    var nameFinders = new Array[TokenNameFinder](models.length)
    for (x <- 0 until nameFinders.length) {

        val model = new TokenNameFinderModel(new File(models(x)))
        nameFinders(x) = new NameFinderME(model)
    }

    // Load regex patterns to tag
    if (patterns != "") {
        nameFinders = nameFinders :+ getRegexNameFinder(patterns)
    }

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
     * Executes the entity grabber using command line options passed in.
     *
     * @param options command line options
     */
    def execute(resumeText: String) {
        // Find the entites and values
        val whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE.tokenize(resumeText)
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
}
