package applicant.etl

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
    "Resume Parser" should "parse a PDF file and return plain text" in {

    val resumeRead = new ResumeReader()

    }
}

