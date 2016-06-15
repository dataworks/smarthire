package applicant.etl

import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.apache.tika.sax.WriteOutContentHandler
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
  val handler : WriteOutContentHandler = new WriteOutContentHandler(-1)
  val metadata : Metadata = new Metadata()
  val context : ParseContext = new ParseContext()

  def extractText (data: DataInputStream) : String = {

    // Apache Tika parser object, auto detects file type
    val myparser : AutoDetectParser = new AutoDetectParser()
    // Input stream for parser, from PortableDataStream data
    val stream : InputStream = data
    // Creates object to hold text ouput from Tika parser
    val handler : WriteOutContentHandler = new WriteOutContentHandler(-1)
    // Creates a object to hold the metadata of the file being parsed
    val metadata : Metadata = new Metadata()
    // Object to pass context information to Tika parser, use to modify parser
    val context : ParseContext = new ParseContext()

    try {
      // Parse text from file and store in hander object
      myparser.parse(stream, handler, metadata, context)
    }
    finally {
      // Close stream after parsing
      stream.close
    }

    return handler.toString()
  }

  def extractMetadata (data: InputStream) : Map[String,String] = {
    val myparser : AutoDetectParser = new AutoDetectParser()
    val stream : InputStream = data


    try {
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

    return metaDataMap
  }

}
