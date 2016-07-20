package applicant.etl

import applicant.nlp._
import applicant.ml.score._
import applicant.ml.regression._
import java.text.DecimalFormat
import java.net.{URL, HttpURLConnection}
import scala.io._
import scala.util._
import scala.collection.mutable.{ListBuffer, Map, LinkedHashMap}
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import scala.collection.JavaConversions._
import org.apache.commons.lang3.text.WordUtils

class ApplicantData {
  var name, recentTitle, recentLocation, recentOrganization, degree, school, email, phone, linkedin, indeed, github, fullText, applicantid: String = ""

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
  var featureScores = ListBuffer[(String, Double)]()
  var githubData = Map[String,String]()
  var score = -1.0
  var gpa = 0.0

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
          "organization" -> otherOrganizationList
        ),
        "url" -> urlList,
        "githubData" -> githubData,
        "resume" -> fullText
      ),
      "summary" -> ResumeSummarizer.summarize(fullText, 150),
      "features" -> featureScores
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
  def apply(taggedEntities: LinkedHashMap[(String, String),(String,String)], applicantid: String, fullText: String, scoreSettings : RegressionSettings): ApplicantData = {

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
        case ("gpa", _) if (app.gpa == 0.0) => app.gpa = if(pair._2.count(_ == '.') > 1) 0.0 else pair._2.toDouble
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

    app.githubData = collection.mutable.Map(ApiMapper.githubAPI(app.github).toSeq: _*)
    if (app.name == notFound) {
      if (app.githubData != collection.mutable.Map() && app.githubData("name") != null && app.githubData("name") != "") {
        app.name = app.githubData("name")
      }
      else if (app.githubData != collection.mutable.Map() && app.githubData("login") != null && app.githubData("login") != "") {
        app.name = app.githubData("login")
      }
      else {
        val textArr = fullText.trim().split("\\s+")
        app.name = textArr(0) + " " + textArr(1)
      }
    }

    app.featureScores = LogisticFeatureGenerator.getEmptyFeatureList(scoreSettings)

    app.name = WordUtils.capitalizeFully(app.name)
    return app
  }

  /**
   * Will check if an option is Some or None and return a string accordingly
   */
  private def getString(value: AnyRef): String = {
    if (value == None) {
      return ""
    }
    return value.asInstanceOf[String]
  }

  /**
   * Will check if an option is Some or None and return a double accordingly
   */
  private def getDouble(value: AnyRef): Double = {
    if (value == None) {
      return 0.0
    }
    return value.asInstanceOf[Double]
  }

  /**
   * Will check if a list option is Some or None and set a list pointer accordingly
   */
  private def getList(value: JListWrapper[String]): ListBuffer[String] = {
    if (value == None) {
      return new ListBuffer[String]()
    }
    else {
      return value.toList.to[ListBuffer]
    }
  }

  /**
   * Creates a new ApplicantData object and loads variables
   *
   * @param elasticMap A map structure returned from querying on the elasticsearch applicant index
   */
  def apply(elasticMap: scala.collection.Map[String, AnyRef]): ApplicantData = {
    val app = new ApplicantData()

    app.applicantid = getString(elasticMap("id"))
    app.name = getString(elasticMap("name"))
    app.score = getDouble(elasticMap("score"))

    elasticMap.get("features") match {
      case Some(features) =>
        app.featureScores = features.asInstanceOf[JListWrapper[(String, Double)]].toList.to[ListBuffer]
      case None =>
    }

    elasticMap.get("currentLocation") match {
      case Some(any) =>
        val locMap = any.asInstanceOf[Map[String, String]]
        app.recentTitle = getString(locMap("title"))
        app.recentLocation = getString(locMap("location"))
        app.recentOrganization = getString(locMap("organization"))
      case None =>
    }

    elasticMap.get("skills") match {
      case Some(any) =>
        val skillMap = any.asInstanceOf[Map[String, JListWrapper[String]]]

        app.languageList = getList(skillMap("language"))
        app.bigDataList = getList(skillMap("bigdata"))
        app.etlList = getList(skillMap("etl"))
        app.databaseList = getList(skillMap("database"))
        app.webappList = getList(skillMap("webapp"))
        app.mobileList = getList(skillMap("mobile"))
      case None =>
    }

    elasticMap.get("education") match {
      case Some(any) =>
        val eduMap = any.asInstanceOf[Map[String, String]]
        app.degree = getString(eduMap("degree"))
        app.school = getString(eduMap("school"))
        app.gpa = getDouble(eduMap("gpa"))
      case None =>
    }

    elasticMap.get("contact") match {
      case Some(any) =>
        val contactMap = any.asInstanceOf[Map[String, String]]
          app.indeed = getString(contactMap("indeed"))
          app.linkedin = getString(contactMap("linkedin"))
          app.github = getString(contactMap("github"))
          app.email = getString(contactMap("email"))
          app.phone = getString(contactMap("phone"))
      case None =>
    }

    elasticMap.get("additionalInfo") match {
      case Some(any) =>
        val infoMap = any.asInstanceOf[Map[String, AnyRef]]
        infoMap.get("pastPositions") match {
          case Some(anyPos) =>
            val pastPosMap = anyPos.asInstanceOf[Map[String, JListWrapper[String]]]
            app.otherTitleList = getList(pastPosMap("title"))
            app.otherLocationList = getList(pastPosMap("location"))
            app.otherOrganizationList = getList(pastPosMap("organization"))
          case None =>
        }
        infoMap.get("url") match {
          case Some(any) =>
            app.urlList = getList(any.asInstanceOf[JListWrapper[String]])
          case None =>
        }
        infoMap.get("githubData") match {
          case Some(anyGit) =>
            app.githubData = (anyGit.asInstanceOf[Map[String, String]])
          case None =>
        }
        app.fullText = getString(infoMap("resume"))
      case None =>
    }

    return app
  }
}
