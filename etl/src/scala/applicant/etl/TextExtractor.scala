package applicant.etl

import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.apache.tika.parser.pdf._
import org.apache.tika.parser.pdf.PDFParserConfig
import org.apache.tika.parser.ocr.TesseractOCRParser
import org.apache.tika.parser.ocr.TesseractOCRConfig
import org.apache.tika.sax.BodyContentHandler
import java.io._
import org.apache.commons.io.FilenameUtils
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest

import scala.collection.mutable.{ListBuffer, Map, LinkedHashMap}

/**
 * Apache Tika functions
 *
 */

object TextExtractor {

  /**
   * Uses Apache Tika library to parse out text from a PDF
   *
   *@param data A PortableDataStream from Spark of a PDF file
   */

  def extractAll(data: InputStream): (String, Map[String, String]) = {
    //Needed if tesseract is not on system path
    val handler : BodyContentHandler = new BodyContentHandler()
    val metadata : Metadata = new Metadata()
    // Apache Tika parser object, auto detects file type
    val myparser : Parser = new AutoDetectParser()
    // Input stream for parser, from PortableDataStream data
    val stream : InputStream = data
    val config : TesseractOCRConfig = new TesseractOCRConfig()
    val pdfConfig : PDFParserConfig = new PDFParserConfig()
    val context : ParseContext = new ParseContext()
    //config.setTesseractPath("/usr/share/tesseract-ocr")
    pdfConfig.setExtractInlineImages(true)
    context.set(classOf[TesseractOCRConfig], config)
    context.set(classOf[PDFParserConfig], pdfConfig)
    context.set(classOf[Parser], myparser)

    try {
      // Parse text from file and store in hander object
      myparser.parse(stream, handler, metadata, context)
    }
    finally {
      stream.close
    }


    var metaDataMap = Map[String,String]()
    val metaDataNames = metadata.names()
    for (name <- metaDataNames) {
      metaDataMap += (name -> metadata.get(name))
    }

    return (handler.toString(), metaDataMap)
  }

}
