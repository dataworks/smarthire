package applicant.etl

import applicant.nlp._

import scala.collection.mutable.{ListBuffer, Map, LinkedHashSet}

/**
 *
 *@version 0.0.1
 *
 */

object EntityMapper {
  /**
   * Pulls in the results from extractText and EntityGrabber and ouputs as formatted map for ES
   *
   * @param taggedEntities A LinkedHashSet object from the EntityGrabber class
   * @param applicantID A String to be used as the applicant's unique ID
   * @param fullText A String of the full parsed resume from extractText
   * @return A map formatted to save to ES as JSON
   */
  def createMap(taggedEntities: LinkedHashSet[(String, String)], applicantID: String, fullText: String): Map[String, Object] = {
    var name, recentTitle, recentLocation, recentOrganization, degree, school, gpa, url, email, phone: String = "not found"
    var score: Double = 0.0
    val languageList: ListBuffer[String] = new ListBuffer[String]()
    val bigDataList: ListBuffer[String] = new ListBuffer[String]()
    val etlList: ListBuffer[String] = new ListBuffer[String]()
    val databaseList: ListBuffer[String] = new ListBuffer[String]()
    val webappList: ListBuffer[String] = new ListBuffer[String]()
    val mobileList: ListBuffer[String] = new ListBuffer[String]()
    val otherTitleList: ListBuffer[String] = new ListBuffer[String]()
    val otherLocationList: ListBuffer[String] = new ListBuffer[String]()
    val otherOrganizationList: ListBuffer[String] = new ListBuffer[String]()

    //degree, location, organization, person, school, title, bigdata, database, etl, webapp, mobile, language, gpa, email, phone, url

    taggedEntities.foreach { pair =>
      pair match {
        case ("degree", _) if (degree == "not found") => degree = pair._2
        case ("location", _) => if (recentLocation == "not found") { recentLocation = pair._2 }
          otherLocationList += pair._2
        case ("organization", _)  => if (recentOrganization == "not found") { recentOrganization = pair._2 }
          otherOrganizationList += pair._2
        case ("person", _) if (name == "not found") => name = pair._2
        case ("school", _) if (school == "not found") => school = pair._2
        case ("title", _) => if (recentTitle == "not found") { recentTitle = pair._2 }
          otherTitleList += pair._2
        case ("bigdata", _) => bigDataList += pair._2
        case ("database", _) => databaseList += pair._2
        case ("etl", _) => etlList += pair._2
        case ("webapp", _) => webappList += pair._2
        case ("mobile", _) => mobileList += pair._2
        case ("language", _) => languageList += pair._2
        case ("gpa", _) if (gpa == "not found") => gpa = pair._2
        case ("email", _) if (email == "not found") => email = pair._2
        case ("phone", _) if (phone == "not found") => phone = pair._2
      }
    }

    val strScore: String = String.valueOf(score)

    if (url.equalsIgnoreCase("not found")) {
      url = ""
    }
    if (gpa.equalsIgnoreCase("not found")) {
      gpa = ""
    }

    val map: Map[String, Object] = Map(
      "id" -> applicantID,
      "name" -> name,
      "score" -> strScore,
      "currentLocation" -> Map(
        "title" -> recentTitle,
        "location" -> recentLocation,
        "organization" -> recentOrganization
      ),
      "skills" -> Map(
          "langage" -> languageList,
          "bigdata" -> bigDataList,
          "etl" -> etlList,
          "database" -> databaseList,
          "webapp" -> webappList,
          "mobile" -> mobileList
      ),
      "education" -> Map(
        "degree" -> degree,
        "school" -> school,
        "gpa" -> gpa
      ),
      "contact" -> Map(
        "url" -> url,
        "email" -> email,
        "phone" -> phone
      ),
      "additionalInfo" -> Map(
        "pastPostions" -> Map(
          "title" -> otherTitleList,
          "location" -> otherLocationList,
          "organization" -> otherOrganizationList
        ),
        "resume" -> fullText
      )
    )

    return map
  }
}
