package applicant.etl

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.scalatest.FlatSpec
import org.scalatest.PrivateMethodTester._
import org.scalatest.Matchers._
import applicant.etl.ResumeReader

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

      val conf = new SparkConf().setMaster("local[*]").setAppName("ResumeReaderSpec")
      val sc = new SparkContext(conf)
      val fileData = sc.binaryFiles("test/scala/applicant/etl/testResume.pdf")

      val outStr = ResumeReader.extractText(fileData.values.first())
      outStr should include ("contribute my experiences and skills to the Information Technology industry")
      sc.stop()

    }
}
