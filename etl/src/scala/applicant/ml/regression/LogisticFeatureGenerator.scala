package applicant.ml.regression

import scala.util.Try
import scala.collection.mutable.HashMap
import applicant.nlp.LuceneTokenizer
import applicant.etl._
import java.util.regex
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.mllib.linalg.{Vectors, Vector}

/**
 * FeatureGenerator
 */
object LogisticFeatureGenerator {

  val locationMap: HashMap[(String, String), (Double, Double)] = {
    val cityFileLoc = "data/citylocations/UsData.txt"

    val result = HashMap[(String, String), (Double, Double)]()
    val lines = scala.io.Source.fromFile(cityFileLoc).getLines()

    for (line <- lines) {
      val splitVals = line.toLowerCase().split("#")//#split
      result += ((splitVals(0), splitVals(1)) -> (splitVals(2).toDouble, splitVals(3).toDouble))
    }

    result
  }

  /**
   * Calculates all of the feature scores and returns a vector of the scores
   *
   * The features are as follows:
   *  1) Number of terms similar to Big Data
   *  2) Number of terms similar to Database Engineering
   *  3) Number of terms similar to ETL Engineering
   *  4) Number of terms similar to Web App Development
   *  5) Number of terms similar to Mobile Development
   *  6) Number of terms similar to common programming languages
   *  7) Measure of distance from recent location
   *  8) Density of contact info
   *  9) The length of the resume
   *  10) The GPA
   *  11) What type and how technical their degree
   *  12) How technical their positions have been
   *
   * @param model A word2VecModel uses to find synonyms
   * @param applicant The applicant whose features are needed
   * @return A vector that corresponds to the feature scores
   */
  def getLogisticFeatureVec(model: Word2VecModel, applicant: ApplicantData): Vector = {
    //first feature (number of synonyms to Java/Spark/Hadoop within resume body)
    val featureArray = scala.collection.mutable.ArrayBuffer.empty[Double]
    // Core key words
    featureArray += keywordSynonyms(w2vSynonymMapper(model, List("Hadoop","Spark","HBase","Hive","Cassandra","MongoDB","Elasticsearch","Docker","AWS"), 5), applicant.fullText)
    featureArray += keywordSynonyms(w2vSynonymMapper(model, List("Oracle","Postgresql","Mysql"), 5), applicant.fullText)
    featureArray += keywordSynonyms(w2vSynonymMapper(model, List("Pentaho","Informatica","Streamsets","Syncsort"), 5), applicant.fullText)
    featureArray += keywordSynonyms(w2vSynonymMapper(model, List("AngularJS","Grails","Spring","Hibernate","node.js"), 5), applicant.fullText)
    featureArray += keywordSynonyms(w2vSynonymMapper(model, List("Android","iOS","Ionic","Cordova","Phonegap"), 0), applicant.fullText)
    featureArray += keywordSynonyms(w2vSynonymMapper(model, List("Java","Javascript","Scala","Groovy","C#","C++","Python","Ruby"), 0), applicant.fullText)

    //distance from Reston VA
    if (applicant.recentLocation == "") {
      featureArray += 0.0
    }
    else {
      featureArray += distanceFinder("Reston,VA", applicant.recentLocation)
    }
    //density of contact info
    featureArray += countContacts(applicant)
    //length of resume
    featureArray += resumeLength(applicant.fullText)
    //gpa value
    if (applicant.gpa == "") {
      featureArray += 3.0
    }
    else {
      featureArray += gpaDouble(applicant.gpa)
    }

    //Measure of degree score
    featureArray += degreeScore(applicant.degree)

    //measure of past titles
    featureArray += pastTitles(applicant.recentTitle, applicant.otherTitleList.toList)

    return Vectors.dense(featureArray.toArray[Double])
  }


  /**
   * Will look through a the resume string seeing if the resume contains at
   *  least 2 of a set of keywords
   *
   * @param w2vmap Word2Vec synonym map
   * @param resume Full string of parsed resume
   * @return First feature score framed to 0-1
   */
  def keywordSynonyms (w2vmap: HashMap[String,Int], resume: String): Double = {
    val tokenizer = new LuceneTokenizer()
    val resumeArray = tokenizer.tokenize(resume) //Converts to lowercase
    var matches : Double = 0.0

    resumeArray.foreach { word =>
      if (w2vmap.contains(word)){
        val currentWordCount = w2vmap(word)
        w2vmap += (word -> (currentWordCount + 1))
      }
    }

    w2vmap.foreach{ case (k,v) =>
      if (v >= 2){
        if (v > 5) {
          matches += 5.0
        }
        else {
          matches += v.toDouble
        }
      }
    }

    val rawScore = matches/(w2vmap.size*1.0)

    return if (rawScore > 1.0) 1.0 else rawScore
  }

  def locationToPair(location: String): (String, String) = {
    val tokens = location.split(",")
    if (tokens.length == 2) {
      val city = tokens(0).trim
      val state = tokens(1).trim

      return (city, state)
    }
    return ("", "")
  }

  /**
   * A backup for finding distance when google decides that we have used enough of their info
   *
   * @param location1 First location
   * @param location2 Second location
   * @return Distance between the two locations in meters
   */
  def backupDistanceFinder (location1: String, location2: String): Double = {
    val loc1Key = locationToPair(location1.toLowerCase())
    val loc2Key = locationToPair(location2.toLowerCase())

    locationMap.get(loc1Key) match {
      case Some(loc1Coords) =>
        locationMap.get(loc2Key) match {
          case Some(loc2Coords) =>

            val result = GeoUtils.haversineEarthDistance(loc1Coords, loc2Coords)

            if (result >= 1000000.0){
              return 0.0
            }
            else {
              return 1 - (result/1000000.0)
            }

          case None =>
            return 0.25
        }
      case None =>
        return 0.25
    }
  }

  /**
   * Finds the distance between two locations using first google and second a list of cities
   *
   * @param location1 First location
   * @param location2 Second location
   * @return Distance between the two locations in meters
   */
  def distanceFinder (location1: String, location2: String): Double = {
    val distance = ApiMapper.googlemapsAPI(location1, location2)
    distance match {
      case Some(distance) =>
        if (distance.toDouble >= 3000000.0){
          return 0.0
        }
        else {
          return 1 - (distance.toDouble/3000000.0)
        }
      case None =>
      return backupDistanceFinder(location1, location2)
    }
  }

  /**
   * Chechs if a string is empty or not
   *
   * @param str String to check if empty or null
   * @return 0 if str is null or empty, 1 otherwise
   */
  def stringCounter(str: String) : Int = {
    if (str != null && !str.isEmpty()) {
      return 1
    }
    else {
      return 0
    }
  }

  /**
   * Counts number of contact information items
   *
   * @param app The applicant being ranked
   * @return The number of contact items found
   */
  def countContacts (app: ApplicantData): Double = {
    val sum = stringCounter(app.linkedin) + stringCounter(app.github) + stringCounter(app.indeed) + app.urlList.length + stringCounter(app.email) + stringCounter(app.phone)
    if (sum >= 5.0) {
      return 1.0
    }
    else {
      return sum.toDouble/5.0
    }
  }

  /**
   * Measures resume length (note: may want to precompile regex if slow)
   *
   * @param resume Full string of parsed resume
   * @return Resume length without punctuation, spaces, or newline characters
   */
  def resumeLength (resume: String): Double = {
    val resumeLength = resume.replaceAll("[^a-zA-Z0-9]+","").length()
    if (resumeLength >= 25000) {
      return 1.0
    }
    else {
      return resumeLength.toDouble / 25000.0
    }
  }

  /**
   * Converts the GPA field stored in ApplicantData to a double
   * (note: nlp gpa parser can be improved by changing gpa in regex.txt to omit the word "GPA")
   * @param gpa gpa string from applicant
   * @return gpa as a double
   */
  def gpaDouble (gpa: String) : Double = {
    try {
      val gpaDbl = gpa.toDouble
      if (gpaDbl >= 4.0) {
        return 1.0
      }
      else {
        return gpaDbl / 4.0
      }
    }
    catch {
      case ex: Exception => return 0.0
    }
  }

  /**
   * Will check what kind of degree the user has and assign score based
   *  on its techiness
   *
   * @param degree The degree field parsed out from the resume
   * @return score of degree
   */
  def degreeScore (degree: String) : Double = {
    var degreeVal = 0.0
    val degreeKeywords : List[String] = List("tech", "computer", "information", "engineer", "c.s.", "program")
    //give point if degree is parsed
    if (degree != "") {
      degreeVal += 1.0
    }
    //give point if degree is masters, else 0.5 for bachelors
    if (degree.toLowerCase.contains("master")) {
      degreeVal += 1.0
    }
    else if(degree.toLowerCase.contains("bachelor")) {
      degreeVal += 0.5
    }
    //give 2 points if tech degreeVal
    if (degreeKeywords.exists(degree.toLowerCase.contains)) {
      degreeVal += 2.0
    }
    return degreeVal / 4.5
  }

  /**
   * Will look through the past positions the user has had and
   *  whether they were tech related
   *
   * @param recentTitle Current position
   * @param pastTitles List of previous positions
   * @return Double representing number of titles and if tech related
   */
  def pastTitles (recentTitle: String, pastTitles: List[String]) : Double = {
    var titleScore = 0.0
    val titleKeywords : List[String] = List("tech", "computer", "information", "engineer", "developer", "software", "analyst")
    if (recentTitle != "") {
      titleScore += 1.0
    }
    titleScore += pastTitles.length
    for (title <- pastTitles) {
      if (titleKeywords.exists(title.toLowerCase.contains)) {
        titleScore += 1.0
      }
    }
    if (titleScore >= 5.0) {
      return 1.0
    }
    else {
      return titleScore/5.0
    }
  }

  /**
   * Creates a hash map of w2v synonyms and booleans
   * @param model A w2v model generated from source data
   * @param terms The search terms to find synonyms for
   * @param synonymCount Number of synonyms to return per term
   * @return A hash map of the synonyms as keys and booleans as values
   */
  def w2vSynonymMapper(model: Word2VecModel, terms: List[String], synonymCount: Int) : HashMap[String,Int] = {
    val map = HashMap.empty[String,Int]
    terms.foreach{ term =>
      try {
        map += (term.toLowerCase() -> 0)

        if (synonymCount != 0) {
          val synonyms = model.findSynonyms(term.toLowerCase(), synonymCount)
          for((synonym, cosineSimilarity) <- synonyms) {
            //Filter out numbers
            if (!Character.isDigit(synonym.charAt(0))) {
              map += (synonym.toLowerCase() -> 0)
            }
          }
        }
      }
      catch {
        //case ise: java.lang.IllegalStateException => println(term + " not found in w2v library")
        case e: Exception => //println("An error occurred finding synonyms for " + term)
      }
    }
    return map
  }

}
