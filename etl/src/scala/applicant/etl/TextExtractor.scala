package applicant.etl

import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.apache.tika.parser.pdf._
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

  def extractAll(data: InputStream): (String, Map[String, String]) = {
    val handler : WriteOutContentHandler = new WriteOutContentHandler(-1)
    val metadata : Metadata = new Metadata()
    val context : ParseContext = new ParseContext()
    // Apache Tika parser object, auto detects file type
    val myparser : AutoDetectParser = new AutoDetectParser()
    // Input stream for parser, from PortableDataStream data
    val stream : InputStream = data

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

  def extractText (data: InputStream) : String = {
    val handler : WriteOutContentHandler = new WriteOutContentHandler(-1)
    val metadata : Metadata = new Metadata()
    val context : ParseContext = new ParseContext()
    // Apache Tika parser object, auto detects file type
    val myparser : AutoDetectParser = new AutoDetectParser()
    // Input stream for parser, from PortableDataStream data
    val stream : InputStream = data

    try {
      // Parse text from file and store in hander object
      myparser.parse(stream, handler, metadata, context)
    }
    finally {
      stream.close
    }

    return handler.toString()
  }

  def extractMetadata (data: InputStream) : Map[String,String] = {
    val handler : WriteOutContentHandler = new WriteOutContentHandler(-1)
    val metadata : Metadata = new Metadata()
    val context : ParseContext = new ParseContext()
    val myparser : AutoDetectParser = new AutoDetectParser()

    try{
      myparser.parse(data, handler, metadata, context)
    }
    finally {
      data.close
    }

    var metaDataMap = Map[String,String]()
    val metaDataNames = metadata.names()
    for (name <- metaDataNames) {
      metaDataMap += (name -> metadata.get(name))
    }

    return metaDataMap
  }

}
