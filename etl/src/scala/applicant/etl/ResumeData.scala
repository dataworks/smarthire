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
import org.apache.pdfbox.pdmodel._
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.rendering.ImageType
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.imageio.stream

import java.io.DataInputStream

/**
 * Class to hold raw resume data
 */
class ResumeData {

  var esId, base64string, filename, extension, text, uploadId : String = ""
  var metaDataMap = scala.collection.mutable.Map[String,String]()

}

object ResumeData {
  /**
   * Creates a new ApplicantData object and loads variables
   * @param fromES Byte array obtained from Elasticsearch uploads index
   * @param entitySet From EntityExtractor
   * @return A new ResumeData object
   */
  def apply(fileName: String, byteArr: Array[Byte], stream: InputStream, uploadid: String): ResumeData = {
    val resume = new ResumeData()

    val streamResult = TextExtractor.extractAll(stream)
    var text = streamResult._1
    val metaDataMap = streamResult._2
    // If text extractor fails to parse text from resume...
    if (text.replaceAll("[^a-zA-Z0-9]+","").length() <= 1) {
      text = ""
      // Convert byte[] to PDDocument
      val pdf = PDDocument.load(new DataInputStream(new ByteArrayInputStream(byteArr)))
      val pdfRenderer = new PDFRenderer(pdf)
      // For each page in PDDocument convert to image and OCR text
      for (page <- 0 to (pdf.getNumberOfPages() - 1)) {
        val pdfPagesAsImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.GRAY)
        val os : ByteArrayOutputStream = new ByteArrayOutputStream()
        ImageIO.write(pdfPagesAsImage, "jpg", os)
        val pgImgArr = os.toByteArray()
        text += TextExtractor.extractAll(new DataInputStream(new ByteArrayInputStream(pgImgArr)))._1
      }

      pdf.close()
    }

    resume.uploadId = uploadid
    resume.esId = Hex.encodeHexString(MessageDigest.getInstance("MD5").digest(byteArr)).toLowerCase()
    resume.text = text
    resume.base64string = Base64.encodeBase64String(byteArr)
    resume.metaDataMap = metaDataMap
    resume.filename = fileName
    resume.extension = FilenameUtils.getExtension(fileName)

    return resume
  }

}
