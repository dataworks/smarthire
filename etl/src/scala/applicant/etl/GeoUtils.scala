package applicant.etl

import java.lang.Math

/**
 * GeoUtils contains methods that deal with calculating geographical distances and the like
 */
object GeoUtils {
  /**
  * Will find the distance between two latitude/longitude points in meters
  *
  * @param loc1coords The latitude and longitude of the first point
  * @param loc2coords The latitude and longitude of the second point
  * @return The distance between the points in meters
  */
  def haversineEarthDistance(loc1Coords: (Double, Double), loc2Coords: (Double, Double)): Double = {
    val r = 6371.0
    val dLat = Math.toRadians(loc2Coords._1 - loc1Coords._1)
    val dLon = Math.toRadians(loc2Coords._2 - loc1Coords._2)
    val a = Math.sin(dLat/2.0) * Math.sin(dLat/2.0) + Math.cos(loc1Coords._1.toRadians) * Math.cos(loc2Coords._1.toRadians) * Math.sin(dLon/2.0) * Math.sin(dLon/2.0)
    val c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0-a))
    return r * c * 1000.0;
  }
}
