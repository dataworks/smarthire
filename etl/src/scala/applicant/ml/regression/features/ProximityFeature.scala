package applicant.ml.regression.features

import applicant.etl.ApplicantData
import applicant.etl.GeoUtils
import applicant.ml.regression.FeatureSetting
import scala.collection.mutable.{ListBuffer, HashMap, Map}

class ProximityFeature(newSetting: FeatureSetting, locationMap: HashMap[(String, String), (Double, Double)]) extends BaseFeature {
  val setting: FeatureSetting = newSetting

  /**
   * Will give a double from 0 - 1 based on distance
   *
   * @param meters the distance in meters
   * @return a double from 0 to 1
   *          0 signifies far away
   *          1 signifies close by
   */
  def scaleDistance(meters: Double, maxDistance: Double): Double = {
    //f(x) = 2.0/(1 + 100^(x/4500000))
    return if (meters >= maxDistance) 0.0 else (2.0/(1 + Math.pow(100, meters/maxDistance)))
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
   *  Will return a score for the proximity of the applicant to the job location
   *
   * @param applicant The applicant whose feature is checked
   */
  def getFeatureScore(applicant: ApplicantData): Double = {
    val valuesMap = setting.values(0).asInstanceOf[Map[String,AnyRef]]
    val location1 = valuesMap("location").asInstanceOf[String]
    val maxDistance = valuesMap("maxDistance").asInstanceOf[Double]
    val location2 = applicant.recentLocation

    //Sanity check
    if (location1 == "" || location2 == "") {
      return 0.0
    }

    val loc1Key = locationToPair(location1.toLowerCase())
    val loc2Key = locationToPair(location2.toLowerCase())

    locationMap.get(loc1Key) match {
      case Some(loc1Coords) =>
        locationMap.get(loc2Key) match {
          case Some(loc2Coords) =>
            val rawResult = GeoUtils.haversineEarthDistance(loc1Coords, loc2Coords)
            return scaleDistance(rawResult, maxDistance)
          case None =>
            return 0.25
        }
      case None =>
        return 0.25
    }
  }
}
