package applicant.nlp

import java.io._

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._
import scala.collection.mutable.LinkedHashSet

/**
 * Scala Test Spec to test the EntityGrabber
 *
 */
 class EntityGrabberSpec extends FlatSpec {
   "Execute" must "grab entities" in {
     val models = List[String]("model/nlp/en-ner-degree.bin", "model/nlp/en-ner-location.bin", "model/nlp/en-ner-organization.bin", "model/nlp/en-ner-person.bin", "model/nlp/en-ner-school.bin", "model/nlp/en-ner-title.bin")
     val patterns = "model/nlp/regex.txt"
     val grabber = new EntityGrabber(models, patterns)

     var filePath: String = "data/test/resume.txt"

     var lines: String = ""
     var br: BufferedReader = new BufferedReader(new FileReader(filePath))
     var line: String = ""
     while ({line = br.readLine(); line != null}) {
        lines += (line + "\n")
     }

     br.close()

     val entities = grabber.extractEntities(lines)

     entities.contains(("person","jason frederick")) mustBe (true)
     entities.contains(("title","web developer")) mustBe (true)
     entities.contains(("organization","american financial group, inc.")) mustBe (true)
     entities.contains(("location","thousand oaks, ca")) mustBe (true)
     entities.contains(("title","etl developer")) mustBe (true)
     entities.contains(("organization","alaska air group, inc.")) mustBe (true)
     entities.contains(("location","columbus, ga")) mustBe (true)
     entities.contains(("webapp","javascript")) mustBe (true)
     entities.contains(("degree","bs biology")) mustBe (true)
     entities.contains(("school","harvard university")) mustBe (true)
  }
}
