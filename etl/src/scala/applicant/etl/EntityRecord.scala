package applicant.etl

import applicant.nlp._
import java.text.DecimalFormat

import scala.collection.mutable.{ListBuffer, Map, LinkedHashMap}

object EntityRecord {
  /**
   * Pulls in the results from extractText and EntityGrabber and ouputs as formatted map for ES
   *
   * @param taggedEntities A LinkedHashSet object from the EntityGrabber class
   * @param applicantID A String to be used as the applicant's unique ID
   * @param fullText A String of the full parsed resume from extractText
   * @return A map formatted to save to ES as JSON
   */
  def create(taggedEntities: LinkedHashMap[(String, String),(String,String)], applicantID: String, fullText: String): Map[String, Object] = {
    var name, recentTitle, recentLocation, recentOrganization, degree, school, gpa, email, phone, notFound, linkedin, indeed, github: String = ""

    val languageList: ListBuffer[String] = new ListBuffer[String]()
    val bigDataList: ListBuffer[String] = new ListBuffer[String]()
    val etlList: ListBuffer[String] = new ListBuffer[String]()
    val databaseList: ListBuffer[String] = new ListBuffer[String]()
    val webappList: ListBuffer[String] = new ListBuffer[String]()
    val mobileList: ListBuffer[String] = new ListBuffer[String]()
    val urlList: ListBuffer[String] = new ListBuffer[String]()
    var score: Double = 0.0
    val otherTitleList: ListBuffer[String] = new ListBuffer[String]()
    val otherLocationList: ListBuffer[String] = new ListBuffer[String]()
    val otherOrganizationList: ListBuffer[String] = new ListBuffer[String]()
    val df: DecimalFormat = new DecimalFormat("#.##")

    //degree, location, organization, person, school, title, bigdata, database, etl, webapp, mobile, language, gpa, email, phone, url

    taggedEntities.values.foreach { pair =>
      pair match {
        case ("degree", _) if (degree == notFound) => (degree = pair._2)
        case ("location", _) => if (recentLocation == notFound) { recentLocation = pair._2 }
          otherLocationList += pair._2
        case ("organization", _)  => if (recentOrganization == notFound) { recentOrganization = pair._2 }
          otherOrganizationList += pair._2
        case ("person", _) if (name == notFound) => name = pair._2
        case ("school", _) if (school == notFound) => school = pair._2
        case ("title", _) => if (recentTitle == notFound) { recentTitle = pair._2 }
          otherTitleList += pair._2
        case ("bigdata", _) => (bigDataList += pair._2, score += 1)
        case ("database", _) => (databaseList += pair._2, score += 1)
        case ("etl", _) => (etlList += pair._2, score += 1)
        case ("webapp", _) => (webappList += pair._2, score += 1)
        case ("mobile", _) => (mobileList += pair._2, score += 1)
        case ("language", _) => (languageList += pair._2, score += 1)
        case ("gpa", _) if (gpa == notFound) => gpa = pair._2
        case ("url", _) => (urlList += pair._2)
        case ("indeed", _) if (indeed == notFound && pair._2.startsWith("http")) => indeed = pair._2
        case ("indeed", _) if (indeed == notFound && pair._2.startsWith("www")) => indeed = "http://" + pair._2
        case ("indeed", _) if (indeed == notFound) => indeed = "http://www." + pair._2
        case ("linkedin", _) if (linkedin == notFound && pair._2.startsWith("http")) => linkedin = pair._2
        case ("linkedin", _) if (linkedin == notFound && pair._2.startsWith("www")) => linkedin = "http://" + pair._2
        case ("linkedin", _) if (linkedin == notFound) => linkedin = "http://www." + pair._2
        case ("github", _) if (github == notFound && pair._2.startsWith("http")) => github = pair._2
        case ("github", _) if (github == notFound && pair._2.startsWith("www")) => github = "http://" + pair._2
        case ("github", _) if (github == notFound) => github = "http://www." + pair._2
        case ("email", _) if (email == notFound) => email = pair._2
        case ("phone", _) if (phone == notFound) => phone = pair._2
        case _ =>
      }
    }

    score = score/10

    if (score > 1)
      score = 1

    val strScore: String = String.valueOf(df.format(score))

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
        "language" -> languageList,
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
        "indeed" -> indeed,
        "linkedin" -> linkedin,
        "github" -> github,
        "email" -> email,
        "phone" -> phone
      ),
      "additionalInfo" -> Map(
        "pastPositions" -> Map(
          "title" -> otherTitleList,
          "location" -> otherLocationList,
          "organization" -> otherOrganizationList,
          "url" -> urlList
        ),
        "resume" -> fullText
      )
    )

    return map
  }
}
