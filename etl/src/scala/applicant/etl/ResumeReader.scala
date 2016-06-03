package applicant.etl

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.apache.tika.sax.WriteOutContentHandler
import java.io._

/**
 *@author Brantley Gilbert
 *
 *@version 0.0.1
 *
 */

object ResumeReader {
  /**
   * Uses Apache Tika library to parse out text from a PDF
   *
   *@param data A PortableDataStream from Spark of a PDF file
   */
  def extractText (data: PortableDataStream) = {

    // Apache Tika parser object, auto detects file type
    val myparser : AutoDetectParser = new AutoDetectParser()
    // Input stream for parser, from PortableDataStream data
    val stream : InputStream = data.open()
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

    // Print handler holding parsed text to console, can modify to
    // return in method
    println(handler.toString())
    println("---------------------------------------------")

  }


  def main(args: Array[String]) {

    /**
     * Main method
     * <p>
     * Spark job to read all PDF files in a directory and use
     * Apache Tika to parse out the text.  Needed to extract
     * usable text from PDF resumes
     * <p>
     *
     * @param args Array of Strings: <Resume Directory> <Spark Master>
     */
    if (args.length < 2) {
      System.err.println("Improper command line arguments.")
      System.err.println("Usage: ResumeReader <Resume Directory> <Spark Master>")
      System.exit(1)
    }

    var directory = args(0)
    var spark_master = args(1)

    //File path, uses local directory now, can change for HFDS-S3
    val filesPath = "file:///" + directory + "*"
    //Create Spark configuration object, need to add Elasticsearch elements
    val conf = new SparkConf().setMaster(spark_master).setAppName("ResumeReader")
    //Create Spark RDD using conf
    val sc = new SparkContext(conf)
    //Create a key-value pair RDD of files within resume directory
    //RDD is an array of tuples (String, PortableDataStream)
    val fileData = sc.binaryFiles(filesPath)
    //Sends value of each key-value pair (PortableDataStream)
    // to extractText function
    fileData.values.foreach( x => extractText(x))
  }
}
