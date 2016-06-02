/************************************
Brantley Gilbert

Spark job w/ Scala

Takes a PDF and uses Apache Tika
to parse out text and write to text file
************************************/
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

    //Write results to file
    val outputfile = new File("~/" + file.getName + ".txt")
    val bw = new BufferedWriter(new FileWriter(outputfile))
    bw.write(handler.toString())
    bw.close()
  }


  def main(args: Array[String]) {
    //Parse PDF using Tika
    val filesPath = "/dataworks/users/william/workspace/git/internship-2016/etl/data/resumes/pdf/0a2e76a5cd1ff3c7.pdf"
    val conf = new SparkConf().setAppName("ResumeReader")
    val sc = new SparkContext(conf)
    val fileData = sc.binaryFiles(filesPath)
    fileData.foreach( x => tikaFunc(x))

  }
}
