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

  var languageList: ListBuffer[String] = new ListBuffer[String]()
  var bigDataList: ListBuffer[String] = new ListBuffer[String]()
  var etlList: ListBuffer[String] = new ListBuffer[String]()
  var databaseList: ListBuffer[String] = new ListBuffer[String]()
  var webappList: ListBuffer[String] = new ListBuffer[String]()
  var mobileList: ListBuffer[String] = new ListBuffer[String]()
  var urlList: ListBuffer[String] = new ListBuffer[String]()
  var otherTitleList: ListBuffer[String] = new ListBuffer[String]()
  var otherLocationList: ListBuffer[String] = new ListBuffer[String]()
  var otherOrganizationList: ListBuffer[String] = new ListBuffer[String]()
  var df: DecimalFormat = new DecimalFormat("#.##")
  var githubData = Map[String,String]()
  val r = scala.util.Random
  var score = r.nextDouble

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
          "url" -> urlList
        ),
        "githubData" -> githubData,
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
  def apply(taggedEntities: LinkedHashMap[(String, String),(String,String)], applicantid: String, fullText: String): ApplicantData = {

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

    app.githubData = ApiMapper.githubAPI(app.github)

    return app
  }

  /**
   * Will check if an option is Some or None and return a string accordingly
   */
  private def getSome(value: Option[String]): String = {
    value match {
      case Some(yay) =>
        return yay
      case None =>
        return ""
    }
  }

  /**
   * Will check if an option is Some or None and return a double accordingly
   */
  private def getSome(value: Option[Double]): Double = {
    value match {
      case Some(yay) =>
        return yay
      case None =>
        return 0.0
    }
  }

  /**
   * Will check if a list option is Some or None and set a list pointer accordingly
   */
  private def getSomeList(list: ListBuffer[String], value: Option[ListBuffer[String]]): ListBuffer[String] = {
    value match {
      case Some(yay) =>
        return yay
      case None =>
        return list
    }
  }

  /**
   * Creates a new ApplicantData object and loads variables
   *
   * @param elasticMap A map structure returned from querying on the elasticsearch applicant index
   */
  def apply(elasticMap: Map[String, Any]): ApplicantData = {
    val app = new ApplicantData()

    app.applicantid = getSome(elasticMap.get("id").asInstanceOf[Option[String]])
    app.name = getSome(elasticMap.get("name").asInstanceOf[Option[String]])
    app.score = getSome(elasticMap.get("score").asInstanceOf[Option[Double]])

    elasticMap.get("currentLocation") match {
      case Some(any) =>
        val locMap = any.asInstanceOf[Map[String, String]]
        app.recentTitle = getSome(locMap.get("title"))
        app.recentLocation = getSome(locMap.get("location"))
        app.recentOrganization = getSome(locMap.get("organization"))
      case None =>
        app.recentTitle = ""
        app.recentLocation = ""
        app.recentOrganization = ""
    }

    elasticMap.get("skills") match {
      case Some(any) =>
        val skillMap = any.asInstanceOf[Map[String, ListBuffer[String]]]

        app.languageList = getSomeList(app.languageList, skillMap.get("language"))
        app.bigDataList = getSomeList(app.bigDataList, skillMap.get("bigdata"))
        app.etlList = getSomeList(app.etlList, skillMap.get("etl"))
        app.databaseList = getSomeList(app.databaseList, skillMap.get("database"))
        app.webappList = getSomeList(app.webappList, skillMap.get("webapp"))
        app.mobileList = getSomeList(app.mobileList, skillMap.get("mobile"))
      case None =>
    }

    elasticMap.get("education") match {
      case Some(any) =>
        val eduMap = any.asInstanceOf[Map[String, String]]
        app.degree = getSome(eduMap.get("degree"))
        app.school = getSome(eduMap.get("school"))
        app.gpa = getSome(eduMap.get("gpa"))
      case None =>
    }

    elasticMap.get("contact") match {
      case Some(any) =>
        val contactMap = any.asInstanceOf[Map[String, String]]
          app.indeed = getSome(contactMap.get("indeed"))
          app.linkedin = getSome(contactMap.get("linkedin"))
          app.github = getSome(contactMap.get("github"))
          app.email = getSome(contactMap.get("email"))
          app.phone = getSome(contactMap.get("phone"))
      case None =>
    }

    elasticMap.get("additionalInfo") match {
      case Some(any) =>
        val infoMap = any.asInstanceOf[Map[String, Any]]
        infoMap.get("pastPositions") match {
          case Some(anyPos) =>
            val pastPosMap = anyPos.asInstanceOf[Map[String, ListBuffer[String]]]
            app.otherTitleList = getSomeList(app.otherTitleList, pastPosMap.get("title"))
            app.otherLocationList = getSomeList(app.otherLocationList, pastPosMap.get("location"))
            app.otherOrganizationList = getSomeList(app.otherOrganizationList, pastPosMap.get("organization"))
            app.urlList = getSomeList(app.urlList, pastPosMap.get("url"))
          case None =>
        }
        infoMap.get("githubData") match {
          case Some(anyGit) =>
            app.githubData = (anyGit.asInstanceOf[Map[String, String]])
          case None =>
            app.githubData = Map[String, String]()
        }
        app.fullText = getSome(infoMap.get("resume").asInstanceOf[Option[String]])
      case None =>
    }

    return app
  }
}
