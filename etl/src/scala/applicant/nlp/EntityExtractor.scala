package applicant.nlp

import java.io.File
import java.io.ByteArrayInputStream
import java.util.regex.Pattern
import java.nio.charset.Charset

import opennlp.tools.namefind.{NameFinderME, NameSample, RegexNameFinder, TokenNameFinder, TokenNameFinderModel}
import opennlp.tools.tokenize.WhitespaceTokenizer
import opennlp.tools.util.{ObjectStream, PlainTextByLineStream, Span}

import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer, Map, LinkedHashMap}
import scala.io.Source

/**
 * Uses entity models to parse information from a resume text string
 *
 * @param modelFiles a list of string paths to NLP models
 * @param patterns a path to a file containing regular expression files
 */
class EntityExtractor(models: Seq[String], patterns: String) {
    //Initialization
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

                // Add regex pattern/type mapping
                val expression: Array[String] = line.split("\\s*=\\s*")
                patterns(expression(0)) = Array(Pattern.compile(expression(1)))
            }
        }

        return new RegexNameFinder(patterns)
    }

    /**
     * Clears adaptive data in nameFinders
     */
    def clearNameFinderData() {
      for (nameFinder <- nameFinders) {
          nameFinder.clearAdaptiveData()
      }
    }

    /**
     * Use the models to grab the entity values from a string
     *
     * @param options command line options
     */
    def extractEntities(text: String): LinkedHashMap[(String, String),(String, String)]  = {
        this.clearNameFinderData()
        val entitySet = LinkedHashMap[(String, String),(String, String)]()

        var line: String = null
        val untokenizedLineStream = new PlainTextByLineStream(new ByteArrayInputStream(text.getBytes()), Charset.forName("UTF-8"))
        while ({ line = untokenizedLineStream.read(); line != null }) {
          if (!line.equals("")) {
            // Find the entites and values
            val whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE.tokenize(line)

            val names: ListBuffer[Span] = new ListBuffer[Span]()
            for (nameFinder <- nameFinders) {
                names.appendAll(nameFinder.find(whitespaceTokenizerLine))
            }

            val reducedNames = NameFinderME.dropOverlappingSpans(names.toArray)
            val nameSample = new NameSample(whitespaceTokenizerLine, reducedNames, false)

            //Put all of the entities into entitySet
            val sentence = nameSample.getSentence()
            val entityNames = nameSample.getNames()

            for (name <- entityNames) {
                // Build and clean entity
                var entity = sentence.slice(name.getStart(), name.getEnd()).mkString(" ")
                entity = entity.replaceAll("\\,$", "")
                if (name.getType == "gpa") {
                  val onlyDouble = entity.replaceAll("[^0-9\\.]+", "")
                  entity = onlyDouble
                }
                entitySet += ((name.getType().toLowerCase, entity.toLowerCase) -> (name.getType(), entity))
            }
          }
        }

        return entitySet
    }

}
