package applicant.etl

import applicant.nlp._
import applicant.ml.score._
import java.text.DecimalFormat
import java.net.{URL, HttpURLConnection}
import scala.io._
import scala.util._
import scala.collection.mutable.{ListBuffer, Map, LinkedHashMap, HashMap}
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}

class ApplicantData {
  var name, recentTitle, recentLocation, recentOrganization, degree, school, gpa, email, phone, linkedin, indeed, github, fullText, applicantid: String = ""

  val languageList: ListBuffer[String] = new ListBuffer[String]()
  val bigDataList: ListBuffer[String] = new ListBuffer[String]()
  val etlList: ListBuffer[String] = new ListBuffer[String]()
  val databaseList: ListBuffer[String] = new ListBuffer[String]()
  val webappList: ListBuffer[String] = new ListBuffer[String]()
  val mobileList: ListBuffer[String] = new ListBuffer[String]()
  val urlList: ListBuffer[String] = new ListBuffer[String]()
  val otherTitleList: ListBuffer[String] = new ListBuffer[String]()
  val otherLocationList: ListBuffer[String] = new ListBuffer[String]()
  val otherOrganizationList: ListBuffer[String] = new ListBuffer[String]()
  val df: DecimalFormat = new DecimalFormat("#.##")
  val githubData = ApiMapper.githubAPI(github)
  val r = scala.util.Random
  val score = r.nextDouble

  def toMap(): Map[String, Any] = {

    val map: Map[String, Any] = Map(
      "id" -> applicantid,
      "name" -> name,
      "score" -> score,
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
          "url" -> urlList,
          "githubData" -> githubData
        ),
        "resume" -> fullText
      )
    )

    return map
  }
}

object ApplicantData {
  /**
   * Creates a new ApplicantData object and loads variables
   *
   * @param taggedEntities A LinkedHashSet object from the EntityGrabber class
   * @param applicantID A String to be used as the applicant's unique ID
   * @param fullText A String of the full parsed resume from extractText
   */
  def apply(taggedEntities: LinkedHashMap[(String, String),(String,String)], applicantid: String, fullText: String) {

    //degree, location, organization, person, school, title, bigdata, database, etl, webapp, mobile, language, gpa, email, phone, url
    val app = new ApplicantData()
    val notFound : String = ""
    app.applicantid = applicantid
    app.fullText = fullText

    taggedEntities.values.foreach { pair =>
      pair match {
        case ("degree", _) if (app.degree == notFound) => (app.degree = pair._2)
        case ("location", _) => if (app.recentLocation == notFound) {app.recentLocation = pair._2 }
          app.otherLocationList += pair._2
        case ("organization", _)  => if (app.recentOrganization == notFound) {app.recentOrganization = pair._2 }
          app.otherOrganizationList += pair._2
        case ("person", _) if (app.name == notFound) => app.name = pair._2
        case ("school", _) if (app.school == notFound) => app.school = pair._2
        case ("title", _) => if (app.recentTitle == notFound) {app.recentTitle = pair._2 }
          app.otherTitleList += pair._2
        case ("bigdata", _) => (app.bigDataList += pair._2)
        case ("database", _) => (app.databaseList += pair._2)
        case ("etl", _) => (app.etlList += pair._2)
        case ("webapp", _) => (app.webappList += pair._2)
        case ("mobile", _) => (app.mobileList += pair._2)
        case ("language", _) => (app.languageList += pair._2)
        case ("gpa", _) if (app.gpa == notFound) => app.gpa = pair._2
        case ("url", _) => (app.urlList += pair._2)
        case ("indeed", _) if (app.indeed == notFound && pair._2.startsWith("http")) => app.indeed = pair._2
        case ("indeed", _) if (app.indeed == notFound && pair._2.startsWith("www")) => app.indeed = "http://" + pair._2
        case ("indeed", _) if (app.indeed == notFound) => app.indeed = "http://www." + pair._2
        case ("linkedin", _) if (app.linkedin == notFound && pair._2.startsWith("http")) => app.linkedin = pair._2
        case ("linkedin", _) if (app.linkedin == notFound && pair._2.startsWith("www")) => app.linkedin = "http://" + pair._2
        case ("linkedin", _) if (app.linkedin == notFound) => app.linkedin = "http://www." + pair._2
        case ("github", _) if (app.github == notFound && pair._2.startsWith("https")) => app.github = pair._2
        case ("github", _) if (app.github == notFound && pair._2.startsWith("http")) => app.github = "https" + pair._2.substring(4)
        case ("github", _) if (app.github == notFound && pair._2.startsWith("www")) => app.github = "https://" + pair._2.substring(3)
        case ("github", _) if (app.github == notFound) => app.github = "https://" + pair._2
        case ("email", _) if (app.email == notFound) => app.email = pair._2
        case ("phone", _) if (app.phone == notFound) => app.phone = pair._2
        case _ =>
      }
    }


  }
}
