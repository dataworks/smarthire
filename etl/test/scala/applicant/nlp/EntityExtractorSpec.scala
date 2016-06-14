package applicant.nlp

import java.io._

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._
import scala.collection.mutable.LinkedHashSet

/**
 * Scala Test Spec to test the EntityGrabber
 *
 */
class EntityExtractorSpec extends FlatSpec {
   val models = List[String]("model/nlp/en-ner-degree.bin", "model/nlp/en-ner-location.bin", "model/nlp/en-ner-organization.bin", "model/nlp/en-ner-person.bin", "model/nlp/en-ner-school.bin", "model/nlp/en-ner-title.bin")
   val patterns = "model/nlp/regex.txt"
   val extractor = new EntityExtractor(models, patterns)

   var filePath: String = "data/test/resume.txt"

   var lines: String = ""
   var br: BufferedReader = new BufferedReader(new FileReader(filePath))
   var line: String = ""
   while ({line = br.readLine(); line != null}) {
      lines += (line + "\n")
   }

   br.close()

   val entities = extractor.extractEntities(lines)

    "EntityExtractor" must "find a name" in {
      entities.contains(("person","jason frederick")) mustBe (true)
    }

    "EntityExtractor" must "find the right title" in {
      entities.contains(("title","web developer")) mustBe (true)
    }

    "EntityExtractor" must "take out the organization" in {
      entities.contains(("organization","american financial group, inc.")) mustBe (true)
    }

    "EntityExtractor" must "detect the location" in {
      entities.contains(("location","thousand oaks, ca")) mustBe (true)
    }

    "EntityExtractor" must "recognize the job" in {
      entities.contains(("title","etl developer")) mustBe (true)
    }

    "EntityExtractor" must "acknowledge Alaska's love of flight" in {
      entities.contains(("organization","alaska air group, inc.")) mustBe (true)
    }

    "EntityExtractor" must "not go to Ohio" in {
      entities.contains(("location","columbus, ga")) mustBe (true)
    }

    "EntityExtractor" must "see the javascript" in {
      entities.contains(("webapp","javascript")) mustBe (true)
    }

    "EntityExtractor" must "recognize a true scientist" in {
      entities.contains(("degree","bs biology")) mustBe (true)
    }

    "EntityExtractor" must "get schooled" in {
      entities.contains(("school","harvard university")) mustBe (true)
    }
}
