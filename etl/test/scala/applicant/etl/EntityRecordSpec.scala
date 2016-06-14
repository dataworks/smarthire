package applicant.etl

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._
import scala.collection.mutable.{LinkedHashMap, ListBuffer, HashMap}

/**
 * Scala Test Spec to test the EntityMapper
 *
 */
 class EntityRecordSpec extends FlatSpec {
   val input: LinkedHashMap[(String, String),(String, String)] = LinkedHashMap[(String, String),(String, String)]()

   input += (("person" -> "jason frederick") -> ("person" -> "Jason Frederick"))
   input += (("title" -> "web develoer") -> ("title" -> "Web Developer"))
   input += (("organization" -> "american financial group, inc.") -> ("organization" -> "American Financial Group, Inc."))
   input += (("location" -> "thousand oaks, ca") -> ("location" -> "Thousand Oaks, CA"))
   input += (("title" -> "etl developer") ->("title" -> "ETL Developer"))
   input += (("organization" -> "alaska air group, inc.") -> ("organization" -> "Alaska Air Group, Inc."))
   input += (("location" -> "columbus, ga") -> ("location" -> "Columbus, GA"))
   input += (("webapp" -> "javascript") -> ("webapp" -> "JavaScript"))
   input += (("degree" -> "bs biology") -> ("degree" -> "BS Biology"))
   input += (("school" -> "harvard university") -> ("school" -> "Harvard University"))

   val map = EntityRecord.create(input, "Wow what a good resume name", "This is totally the text that gave us these entities :D")

   "EntityRecord" must "store the name" in {
     map.get("name").get mustBe ("Jason Frederick")
   }

   "EntityRecord" must "store the first title" in {
     map.get("currentLocation").get.asInstanceOf[HashMap[String, String]].get("title") mustBe (Some("Web Developer"))
   }

   "EntityRecord" must "store the first location" in {
     map.get("currentLocation").get.asInstanceOf[HashMap[String, String]].get("location") mustBe (Some("Thousand Oaks, CA"))
   }

   "EntityRecord" must "store the first organization" in {
     map.get("currentLocation").get.asInstanceOf[HashMap[String, String]].get("organization") mustBe (Some("American Financial Group, Inc."))
   }

   "EntityRecord" must "store the first degree" in {
     map.get("education").get.asInstanceOf[HashMap[String, String]].get("degree") mustBe (Some("BS Biology"))
   }

   "EntityRecord" must "store the first school" in {
     map.get("education").get.asInstanceOf[HashMap[String, String]].get("school") mustBe (Some("Harvard University"))
   }

   "EntityRecord" must "add JavaScript to the webapp list" in {
     map.get("skills").get.asInstanceOf[HashMap[String, ListBuffer[String]]].get("webapp").get.contains("JavaScript") mustBe (true)
   }

   "EntityRecord" must "store the resume text" in {
     map.get("additionalInfo").get.asInstanceOf[HashMap[String, String]].get("resume") mustBe (Some("This is totally the text that gave us these entities :D"))
   }
}
