package applicant.etl

import java.io._

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._
import org.scalatest.PrivateMethodTester._
import org.scalatest.BeforeAndAfterAll

/**
 * Test class for TextExtractor
 *
 */
class TextExtractorSpec extends FlatSpec with BeforeAndAfterAll {
  /**
   * Scala Test Spec to test the TextExtractor object.
   */
  "Text Extractor" must "parse a PDF file and return plain text" in {

    var filePath: String = "data/test/resume.txt"

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
    val fileData = sc.binaryFiles("data/test/resume.pdf")

    var text: String = TextExtractor.extractAll(fileData.values.first().open())._1

    text = text.replace(" ", "").replace("\n", "").replace("-", "")

    text mustEqual lines
    sc.stop()
  }
}
