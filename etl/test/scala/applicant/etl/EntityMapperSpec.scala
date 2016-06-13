package applicant.etl

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._
import scala.collection.mutable.{LinkedHashSet, ListBuffer, HashMap}

/**
 * Scala Test Spec to test the EntityMapper
 *
 */
 class EntityMapperSpec extends FlatSpec {
   "CreateMap" must "uuh create a map" in {
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

     val map = EntityMapper.createMap(input, "Wow what a good resume name", "This is totally the text that gave us these entities :D")

      for (item <- map) {
        println(item)
      }

     map.get("name").get mustBe ("Jason Frederick")
     map.get("currentLocation").get.asInstanceOf[HashMap[String, String]].get("title") mustBe (Some("Web Developer"))
     map.get("currentLocation").get.asInstanceOf[HashMap[String, String]].get("location") mustBe (Some("Thousand Oaks, CA"))
     map.get("currentLocation").get.asInstanceOf[HashMap[String, String]].get("organization") mustBe (Some("American Financial Group, Inc."))
     map.get("education").get.asInstanceOf[HashMap[String, String]].get("degree") mustBe (Some("BS Biology"))
     map.get("education").get.asInstanceOf[HashMap[String, String]].get("school") mustBe (Some("Harvard University"))
     map.get("skills").get.asInstanceOf[HashMap[String, ListBuffer[String]]].get("webapp").get.contains("JavaScript") mustBe (true)
     map.get("additionalInfo").get.asInstanceOf[HashMap[String, String]].get("resume") mustBe (Some("This is totally the text that gave us these entities :D"))
  }
}
