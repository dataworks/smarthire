package applicant.nlp

import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern

import opennlp.tools.namefind.{NameFinderME, NameSample, RegexNameFinder, TokenNameFinder, TokenNameFinderModel}
import opennlp.tools.tokenize.WhitespaceTokenizer
import opennlp.tools.util.{ObjectStream, PlainTextByLineStream, Span}

import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer, Map, LinkedHashSet}
import scala.io.Source

/**
 * Uses entity models to parse information from a resume text string
 *
 * @param modelFiles a list of string paths to NLP models
 * @param patterns a path to a file containing regular expression files
 */
class EntityGrabber(models: Seq[String], patterns: String) {
    //Initialization
    // Load trained models to tag
    var nameFinders = new Array[TokenNameFinder](models.length)
    var entitySet = LinkedHashSet[(String, String)]()
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
     * Use the models to grab the entity values from a string
     *
     * @param options command line options
     */
    def load(resumeText: String) = {
        //Clear the internal set
        entitySet.clear()

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

        //Put all of the entities into entitySet
        val sentence = nameSample.getSentence()
        val entityNames = nameSample.getNames()

        for (name <- entityNames) {
            // Build and clean entity
            var entity = sentence.slice(name.getStart(), name.getEnd()).mkString(" ")
            entity = entity.replaceAll("\\,$", "")

            entitySet += (name.getType() -> entity)
        }
    }

    /**
     * Will grab all values of an entity type. Make sure that the entities are loaded first.
     *
     * @param entityType what entity type you want to query
     * @return A list of entity values
     */
    def query(entityType: String): ListBuffer[String] = {
      val result = ListBuffer[String]()

      for (pair <- entitySet) {
        if (pair._1 == entityType) {
          result += pair._2
        }
      }

      return result
    }
}
