package applicant.ml.score

import scala.collection.mutable.HashMap
import applicant.nlp.LuceneTokenizer
import applicant.etl._
//import java.lang._
import java.util.regex
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.mllib.linalg.{Vectors, Vector}

/**
 * FeatureGenerator
 */
object FeatureGenerator {


  /**
   * Calculates all of the feature scores and returns a vector of the scores
   *
   * @param model A word2VecModel uses to find synonyms
   * @param applicant The applicant whose features are needed
   * @return A vector that corresponds to the feature scores
   */
  def getFeatureVec(model: Word2VecModel, applicant: ApplicantData): Vector = {
    //first feature (number of synonyms to Java/Spark/Hadoop within resume body)
    val featureArray = scala.collection.mutable.ArrayBuffer.empty[Double]
    val synonymMap = w2vSynonymMapper(model, List("Java", "Spark", "Hadoop"), 20)
    featureArray += keywordSynonyms(synonymMap, applicant.fullText)
    //second feature (distance from Reston VA)
    if (applicant.recentLocation == "") {
      featureArray += 0.0
    }
    else {
      featureArray += distanceFinder("Reston,VA", applicant.recentLocation)
    }
    //third feature (density of contact info)
    featureArray += countContacts(applicant)
    //fourth feature (length of resume)
    featureArray += resumeLength(applicant.fullText)
    //fifth feature (gpa value)
    if (applicant.gpa == "") {
      featureArray += 3.0
    }
    else {
      featureArray += gpaDouble(applicant.gpa)
    }

    featureArray += degreeScore(applicant.degree)

    featureArray += pastTitles(applicant.recentTitle, applicant.otherTitleList.toList)


    return Vectors.dense(featureArray.toArray[Double])
  }


  /**
   * Calculates first feature
   *
   * @param w2vmap Word2Vec synonym map
   * @param resume Full string of parsed resume
   * @return First feature score framed to 0-1
   */
  def keywordSynonyms (w2vmap: HashMap[String,Boolean], resume: String): Double = {
    val tokenizer = new LuceneTokenizer()
    val resumeArray = tokenizer.tokenize(resume)
    var matches : Double = 0.0

    resumeArray.foreach { word =>
      if (w2vmap.contains(word)){
        w2vmap += (word -> true)
      }
    }

    w2vmap.foreach{ case (k,v) =>
      if (v == true){
        matches += 1
        w2vmap += (k -> false)
      }
    }

    val featuresScore = matches
    return featuresScore/w2vmap.size
  }

  /**
   * Second feature
   *
   * @param location1 First location
   * @param location2 Second location
   * @return Distance between the two locations in meters
   */
  def distanceFinder (location1: String, location2: String): Double = {
    val distance = ApiMapper.googlemapsAPI(location1, location2)
    distance match {
      case Some(distance) =>
        if (distance.toDouble >= 3000000){
          return 0.0
        }
        else {
          return 1 - (distance.toDouble/3000000)
        }
      case None =>
        return 0.0
    }
  }

  /**
   * Third feature, Counts number of contact information items
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
   * Fourth feature, Measures resume length (note: may want to precompile regex if slow)
   *
   * @param resume Full string of parsed resume
   * @return Resume length without punctuation, spaces, or newline characters
   */
  def resumeLength (resume: String): Double = {
    val resumeLength = resume.replaceAll("[^a-zA-Z0-9]+","").length()
    if (resumeLength >= 5000) {
      return 1.0
    }
    else
    {
      return resumeLength.toDouble / 5000
    }
  }

  /**
   * Fifth feature, Converts the GPA field stored in ApplicantData to a double
   * (note: nlp gpa parser can be improved by changing gpa in regex.txt to omit the word "GPA")
   * @param gpa gpa string from applicant
   * @return gpa as a double
   */
  def gpaDouble (gpa: String) : Double = {
    val arrStr : Array[String] = gpa.split(" ")
    val gpaDbl = arrStr(1).toDouble
    if (gpaDbl >= 4.0) {
      return 1.0
    }
    else {
      return gpaDbl / 4.0
    }
  }

  /**
   * Sixth feature
   * @param degree The degree field parsed out from the resume
   * @return score of degree
   */
  def degreeScore (degree: String) : Double = {
    var degreeVal = 0.0
    val degreeKeywords : List[String] = List("tech", "computer", "information", "engineer", "c.s.", "program")
    //give point if degree is parsed
    if (degree != "") {
      degreeVal += 1
    }
    //give point if degree is masters, else 0.5 for bachelors
    if (degree.toLowerCase.contains("master")) {
      degreeVal += 1
    }
    else if(degree.toLowerCase.contains("bachelor")) {
      degreeVal += 0.5
    }
    //give 2 points if tech degreeVal
    if (degreeKeywords.exists(degree.toLowerCase.contains)) {
      degreeVal += 2
    }
    return degreeVal / 4.5
  }

  /**
   * Seventh feature
   * @param recentTitle Current position
   * @param pastTitles List of previous positions
   * @return Double representing number of titles and if tech related
   */
  def pastTitles (recentTitle: String, pastTitles: List[String]) : Double = {
    var titleScore = 0.0
    val titleKeywords : List[String] = List("tech", "computer", "information", "engineer", "developer", "software", "analyst")
    if (recentTitle != "") {
      titleScore += 1
    }
    titleScore += pastTitles.length
    for (title <- pastTitles) {
      if (titleKeywords.exists(title.contains)) {
        titleScore += 1
      }
    }
    if (titleScore >= 10) {
      return 1.0
    }
    else {
      return titleScore/10
    }
  }

  /**
   * Creates a hash map of w2v synonyms and booleans
   * @param model A w2v model generated from source data
   * @param terms The search terms to find synonyms for
   * @param synonymCount Number of synonyms to return per term
   * @return A hash map of the synonyms as keys and booleans as values
   */
  def w2vSynonymMapper(model: Word2VecModel, terms: List[String], synonymCount: Int) : HashMap[String,Boolean] = {
    val map = HashMap.empty[String,Boolean]
    terms.foreach{ term =>
      map += (term -> false)
      val synonyms = model.findSynonyms(term.toLowerCase(), synonymCount)
      for((synonym, cosineSimilarity) <- synonyms) {
        map += (synonym -> false)
      }
    }
    return map
  }

  /**
   * Helper function for thirdFeature
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
}
