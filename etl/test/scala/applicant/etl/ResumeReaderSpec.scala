package applicant.etl

import java.io._

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._
import org.scalatest.PrivateMethodTester._

/**
 *@author Brantley Gilbert
 *
 *@version 0.0.1
 *
 */
class ResumeReaderSpec extends FlatSpec {
  /**
   * Scala Test Spec to test the ResumeReader object.
   */
  "Resume Parser" must "parse a PDF file and return plain text" in {

    var filePath: String = "test/scala/applicant/nlp/resume.txt"

    var lines: String = ""
    var br: BufferedReader = new BufferedReader(new FileReader(filePath))
    var line: String = ""
    while ({line = br.readLine(); line != null}) {
      lines += (line + "\n")
    }

    br.close()

    lines = lines.replace(" ", "").replace("\n", "").replace("-", "")

    val conf = new SparkConf().setMaster("local[*]").setAppName("ResumeReaderSpec")
    val sc = new SparkContext(conf)
    val fileData = sc.binaryFiles("test/scala/applicant/etl/testResume.pdf")

    var text: String = ResumeReader.extractText(fileData.values.first())

    text = text.replace(" ", "").replace("\n", "").replace("-", "")

    println(lines)
    println()
    println(text)

    text mustEqual lines
    sc.stop()

    }
}
