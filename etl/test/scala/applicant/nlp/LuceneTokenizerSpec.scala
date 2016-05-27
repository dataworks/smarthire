package applicant.nlp

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._

/**
 * Scala Test Spec to test the LuceneTokenizerSpec
 */
class LuceneTokenizerSpec extends FlatSpec {
    "Tokenize" must "parse tokens" in {
        val tokenizer = new LuceneTokenizer()

        tokenizer.tokenize("You are a Java Developer") must equal (Array("you", "java", "developer"))
        tokenizer.tokenize("Java, C++, Groovy, Apache Spark") must equal (Array("java", "c", "groovy", "apache", "spark"))
    }
}
