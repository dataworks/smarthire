/************************************
Brantley Gilbert

Spark job w/ Scala

Takes a PDF and uses Apache Tika
to parse out text and write to text file
************************************/

package applicant.etl

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.apache.tika.sax.WriteOutContentHandler
import java.io._

object ResumeReader {

  def tikaFunc (a: (String, PortableDataStream)) = {

    val file : File = new File(a._1.drop(5))
    val myparser : AutoDetectParser = new AutoDetectParser()
    val stream : InputStream = new FileInputStream(file)
    val handler : WriteOutContentHandler = new WriteOutContentHandler(-1)
    val metadata : Metadata = new Metadata()
    val context : ParseContext = new ParseContext()

    myparser.parse(stream, handler, metadata, context)

    stream.close

    println(handler.toString())
    println("---------------------------------------------")
    /*
    //Write results to file
    val outputfile = new File("~/")
    val bw = new BufferedWriter(new FileWriter(outputfile))
    bw.write(handler.toString())
    bw.close()
    */
  }


  def main(args: Array[String]) {

    if (args.length < 1) {
      System.err.println("Improper command line arguments")
      System.exit(1)
    }

    var directory = args(0)

    //Parse PDF using Tika
    val filesPath = "file:///" + directory + "*"
    val conf = new SparkConf().setMaster("local[*]").setAppName("ResumeReader")
    val sc = new SparkContext(conf)
    val fileData = sc.binaryFiles(filesPath)
    fileData.foreach( x => tikaFunc(x))

  }
}
