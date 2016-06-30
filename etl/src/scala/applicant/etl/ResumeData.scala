package applicant.etl

import applicant.nlp._
import applicant.ml.score._
import scala.io._
import java.io._
import scala.util._
import scala.collection.mutable.LinkedHashMap
import org.apache.spark.input.PortableDataStream
import org.apache.commons.codec.binary.{Hex, Base64}
import java.security.MessageDigest
import org.apache.commons.io.FilenameUtils

/**
 * Class to hold raw resume data
 */
class ResumeData {

  var esId, base64string, filename, extension, text : String = ""
  var metaDataMap = scala.collection.mutable.Map[String,String]()

}

object ResumeData {
  /**
   * Creates a new ApplicantData object and loads variables
   * @param fromES Byte array obtained from Elasticsearch uploads index
   * @param entitySet From EntityExtractor
   * @return A new ResumeData object
   */
  def apply(fileName: String, byteArr: Array[Byte]): ResumeData = {
    val resume = new ResumeData()
    val stream : ByteArrayInputStream = new ByteArrayInputStream(byteArr)

    resume.esId = Hex.encodeHexString(MessageDigest.getInstance("MD5").digest(byteArr)).toLowerCase()
    resume.text = TextExtractor.extractText(stream)
    resume.base64string = Base64.encodeBase64String(byteArr)
    resume.metaDataMap = TextExtractor.extractMetadata(stream)
    resume.filename = fileName
    resume.extension = resume.metaDataMap("Content-Type")

    return resume
  }


}
