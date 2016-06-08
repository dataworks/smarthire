package applicant.etl

import applicant.nlp._

import scala.collection.mutable.{ListBuffer, Map, LinkedHashSet}

/**
 *@author Brantley
 *
 *@version 0.0.1
 *
 */

object EntityMapper {
  def createMap(taggedEntities: LinkedHashSet[(String, String)], applicantID: String, fullText: String): Map[String, Object] = {
    val map: Map[String, Object] = Map()
    map += ("test" -> "requires test text")


    return map
  }
}
