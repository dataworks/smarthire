package applicant.ml.regression

import scala.util.Try
import scala.collection.mutable.{ListBuffer, Map, HashMap}
import applicant.nlp.LuceneTokenizer
import applicant.etl._
import applicant.ml.naivebayes._
import java.util.regex
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel, IDFModel}
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.mllib.classification.{NaiveBayesModel, NaiveBayes}

import org.slf4j.{Logger, LoggerFactory}

object LogisticFeatureGenerator {
  private var generator: LogisticFeatureGenerator = null

  //Feature List
  val featureList = List("Relevance", "Big Data", "Database Engineering", "ETL Engineering", "Web App Development", "Mobile Development", "Common Programming Languages", "Distance from Job Site", "Amount of Contact Info", "Resume Length", "Education/Work Background")

  private def getGenerator(): LogisticFeatureGenerator = {
    if (this.generator == null) {
      this.generator = new LogisticFeatureGenerator()
    }
    return this.generator
  }

  def getLogisticFeatureVec(wordModel: Word2VecModel, bayesModel: NaiveBayesModel, idfModel: IDFModel, applicant: ApplicantData): Vector = {
    val generator = getGenerator()
    return generator.getLogisticFeatureVec(wordModel, bayesModel, idfModel, applicant)
  }

  /**
   * Will return a map of feature names with zeros for scores
   *
   * @return a map of features to 0.0
   */
  def getEmptyFeatureMap(): Map[String, Double] = {
    return Map(featureList.map ( feature => (feature, 0.0) ) : _*)
  }

  /**
   * Will return a map of feature names with the provided values
   *
   * @param vec A vector of feature scores that are to be associated
   *              with their feature names
   * @return A map of feature names with their score
   */
  def getPopulatedFeatureMap(vec: Vector): Map[String, Double] = {
    val featureVals = vec.toArray
    return Map((featureList zip featureVals) : _*)
  }
}

/**
 * FeatureGenerator
 */
class LogisticFeatureGenerator {

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

  //Position keywords used to add to experience
  val titleKeywords : List[String] = List("technology", "computer", "information", "engineer", "developer", "software", "analyst", "application", "admin")

  //Degree keywords used to scale the gpa
  val degreeKeywords : List[String] = List("tech", "computer", "information", "engineer", "c.s.", "programming", "I.S.A.T.")

  //logger
  val log: Logger = LoggerFactory.getLogger(getClass())


  /**
   * Calculates all of the feature scores and returns a vector of the scores
   *
   * The features are as follows:
   *  1) Result from testing against the premade NaiveBayesModel
   *  2) Number of terms similar to Big Data
   *  3) Number of terms similar to Database Engineering
   *  4) Number of terms similar to ETL Engineering
   *  5) Number of terms similar to Web App Development
   *  6) Number of terms similar to Mobile Development
   *  7) Number of terms similar to common programming languages
   *  8) Measure of distance from recent location
   *  9) Density of contact info
   *  10) The length of the resume
   *  11) Strength of previous history (positions, gpa, school)
   *
   * @param model A word2VecModel uses to find synonyms
   * @param applicant The applicant whose features are needed
   * @return A vector that corresponds to the feature scores
   */
  def getLogisticFeatureVec(wordModel: Word2VecModel, bayesModel: NaiveBayesModel, idfModel: IDFModel, applicant: ApplicantData): Vector = {
    val featureArray = scala.collection.mutable.ArrayBuffer.empty[Double]
    //NaiveBayesScore
    featureArray += naiveBayesTest(bayesModel, idfModel, applicant)
    // Core key words
    featureArray += keywordSearch(List("Spark","Hadoop","HBase","Hive","Cassandra","MongoDB","Elasticsearch","Docker","AWS","HDFS","MapReduce","Yarn","Solr","Avro","Lucene","Kibana", "Kafka"), applicant.fullText)
    featureArray += keywordSearch(List("Oracle","Postgresql","Mysql","SQL"), applicant.fullText)
    featureArray += keywordSearch(List("Pentaho","Informatica","Streamsets","Syncsort"), applicant.fullText)
    featureArray += keywordSearch(List("AngularJS","Javascript","Grails","Spring","Hibernate","node.js","CSS","HTML"), applicant.fullText)
    featureArray += keywordSearch(List("Android","iOS","Ionic","Cordova","Phonegap"), applicant.fullText)
    featureArray += keywordSearch(List("Java","Scala","Groovy","C","Python","Ruby","Haskell"), applicant.fullText)

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

    featureArray += history(applicant)

    return Vectors.dense(featureArray.toArray[Double])
  }

  /**
   * Will check the word counts in the resume against the premade naiveBayesModel
   *
   * @param model The NaiveBayesModel to test against
   * @param applicant The applicant who needs a score
   * @return The score from the test against the model
   */
  def naiveBayesTest(model: NaiveBayesModel, idfModel: IDFModel, applicant: ApplicantData): Double = {
    val tokenList = LuceneTokenizer.getTokens(applicant.fullText)
    var scores = new ListBuffer[Double]()

    tokenList.foreach { tokens =>
      val score = NaiveBayesHelper.predictSingleScore(model, NaiveBayesFeatureGenerator.getAdjustedFeatureVec(tokens, idfModel))
      scores += score

      if (score < 0.95 && score > 0.05)
        log.debug("  Score = " + score + ", Tokens = " + tokens)
    }

    // Filter overconfident scores. Model confidence with vary more with larger training sets.
    scores = scores.filter { score =>
        score < 0.95 && score > 0.05
    }

    var result = 0.0
    if (scores.length > 0) {
      result = scores.sum / scores.length
    }

    log.debug("---------Result = " + result)

    return result
  }

  /**
   * Will look through a the resume string seeing if the resume contains at
   *  least 2 of a set of keywords
   *
   * @param keywordList A list of terms that are searched
   * @param resume Full string of parsed resume
   * @return First feature score framed to 0-1
   */
  def keywordSearch (keywordList: List[String], resume: String): Double = {
    val tokenizer = new LuceneTokenizer()
    val resumeArray = tokenizer.tokenize(resume) //Converts to lowercase
    var matches : Double = 0.0

    val map = HashMap.empty[String, Int]

    for (item <- keywordList) {
      map += (item.toLowerCase -> 0)
    }

    resumeArray.foreach { word =>
      if (map.contains(word)){
        val currentWordCount = map(word)
        map += (word -> (currentWordCount + 1))
      }
    }

    map.foreach{ case (k,v) =>
      if (v >= 2){
        if (v > 5) {
          matches += 5.0
        }
        else {
          matches += v.toDouble
        }
      }
    }

    val rawScore = matches/(map.size*4.0)

    return if (rawScore > 1.0) 1.0 else rawScore
  }

  /**
   * Will convert a location string into the city and state
   *
   * @param location The loaction string
   * @return A pair of the city name and state name
   */
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
   * Will look through a list of keywords and check if the search string contains
   *  any of them
   */
  private def stringListContainment(search: String, lst: Seq[String]): Boolean = {
    val check = search.toLowerCase
    for (item <- lst) {
      if (check.contains(item)) {
        return true
      }
    }
    return false
  }

  /**
   * Will check if a position string contains certain keywords
   *  that indicate if it is a tech position
   */
  def checkPosition(currentScore: Double, pos: String): Double = {
    if (stringListContainment(pos, titleKeywords)) {
      return currentScore + 1.0
    }
    return currentScore
  }

  /**
   * Will check if a degree string contains certain keywords
   *  that indicate if it is a tech degree
   */
  def checkDegree(deg: String): Boolean = {
    return stringListContainment(deg, degreeKeywords)
  }

  /**
   * Will scale a GPA by a specified amount and adjust
   *  it extra so that very low gpas do not give much credit
   */
  private def gpaScaler(gpa: Double, scale: Double): Double = {
    return (gpa*gpa*scale)/4.0
  }

  /**
   * Will take gpa, education, and past titles all into account
   *
   * Since gpa is often not included once experience is gained,
   *  a score for gpa is calculated, and a score for experience is
   *    calculated, and the higher score is the one that is chosen.
   *
   * Because both education and gpa are both related to school,
   *  the type of degree will scale the gpa
   *
   * @param applicant The applicant that is to be judged
   * @return A double corresponding to the level of historical aptitutde
   */
  def history(applicant: ApplicantData): Double = {
    var rawGPA = applicant.gpa

    //Scale the gpa by the type of degree
    if (checkDegree(applicant.degree)) {
      rawGPA = gpaScaler(rawGPA, 0.5)
    } else {
      rawGPA *= gpaScaler(rawGPA, 0.25)
    }

    var positionScore = 0.0
    positionScore = checkPosition(positionScore, applicant.recentTitle)
    for (position <- applicant.otherTitleList) {
      positionScore = checkPosition(positionScore, applicant.recentTitle)
    }

    val maxScore = Math.max(rawGPA, positionScore) / 4.0

    return if (maxScore > 1.0) 1.0 else maxScore
  }
}
