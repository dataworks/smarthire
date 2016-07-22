package applicant.etl

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions.JListWrapper

object EsUtils {
  /**
   * Will check if an option is Some or None and return a string accordingly
   */
  def getString(value: AnyRef): String = {
    if (value == None) {
      return ""
    }
    return value.asInstanceOf[String]
  }

  /**
   * Will check if an option is Some or None and return a double accordingly
   */
  def getDouble(value: AnyRef): Double = {
    if (value == None) {
      return 0.0
    }
    return value.asInstanceOf[Double]
  }

  /**
   * Will check if a list option is Some or None and set a list pointer accordingly
   */
  def getList(value: AnyRef): ListBuffer[String] = {
    if (value == None) {
      return ListBuffer()
    }
    return value.asInstanceOf[JListWrapper[String]].toList.to[ListBuffer]
  }

  /**
   * Will check if a boolean is None and
   */
  def getBool(value: AnyRef): Boolean = {
    if (value == None) {
      return true
    }
    return value.asInstanceOf[Boolean]
  }

  /**
   * Will check if a list of pair of string and list is None and convert it accordingly
   */
  def getMap(value: AnyRef): Map[String, List[String]] = {
    if (value == None) {
      return Map.empty[String, List[String]]
    }
    val intermediateMap = value.asInstanceOf[Map[String, JListWrapper[String]]]
    return intermediateMap.map { case (str, lst) =>
      (str, lst.toList)
    }
  }
}
