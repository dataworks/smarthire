package applicant.etl

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._
import scala.collection.mutable.{LinkedHashSet, ListBuffer, HashMap}

/**
 * Scala Test Spec to test the EntityMapper
 *
 */
 class EntityRecordSpec extends FlatSpec {
   val input: LinkedHashSet[(String, String)] = LinkedHashSet[(String, String)]()

   input += ("person" -> "Jason Frederick")
   input += ("title" -> "Web Developer")
   input += ("organization" -> "American Financial Group, Inc.")
   input += ("location" -> "Thousand Oaks, CA")
   input += ("title" -> "ETL Developer")
   input += ("organization" -> "Alaska Air Group, Inc.")
   input += ("location" -> "Columbus, GA")
   input += ("webapp" -> "JavaScript")
   input += ("degree" -> "BS Biology")
   input += ("school" -> "Harvard University")

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
