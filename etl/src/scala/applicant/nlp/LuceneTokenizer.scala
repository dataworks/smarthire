package applicant.nlp

import java.io.StringReader
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import scala.collection.mutable.ListBuffer

/**
 * Tokenizes text using Lucene Analyzers.
 */
class LuceneTokenizer {
    /**
     * Tokenizes a String.
     *
     * @param string input string
     * @return Seq[String]
     */
    def tokenize(string: String): Seq[String] = {
        var result = new ListBuffer[String]()
        var stream = new StandardAnalyzer().tokenStream(null, new StringReader(string))
        stream.reset()
        while (stream.incrementToken()) {
            result += stream.getAttribute(classOf[CharTermAttribute]).toString()
        }

        return result
    }
}
