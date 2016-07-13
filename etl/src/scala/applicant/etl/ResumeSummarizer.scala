package applicant.etl

import scala.io._
import applicant.nlp.LuceneTokenizer
import scala.collection.mutable.{LinkedHashSet, LinkedHashMap, HashMap, ListBuffer}

/**
 * IN DEVELOPMENT
 *
 */

object ResumeSummarizer {

  /**
   * Foo
   *
   *@param
   */

  def summarize(resume : String, charLimit : Int) : String = {
    val mostFreqWords : HashMap[String, Int] = getMostFreqWords(100, resume)
    //o
    val regEx = "(?m)((\\.|!|\\?)+(\\s|\\z))|(\n\n)|([→•●〓♦➢>○❾◆◦✓·°❖∗\\*])|(^-)|(^–)|((?=^[A-Z]))"
    val originalSentences = resume.split(regEx)
    val scoredSentences : HashMap[String, Int] = new HashMap()
    val outputSentences: ListBuffer[String] = new ListBuffer[String]
    var sentenceIndex : Int = 0
    var sentencesLength : Int = 0

    for (sentence <- originalSentences) {
        val tokens = LuceneTokenizer.tokenize(sentence)
        var score = 0
        for (token <- tokens) {
          // Calculate weight of each token
          score += mostFreqWords.getOrElse(token, 0)
        }

        if (score > 0) {
          scoredSentences.put(sentence, score)
        }
    }

    val sortedSentences = LinkedHashMap(scoredSentences.toSeq.sortWith(_._2 > _._2): _*)

    for (sentence <- sortedSentences if sentencesLength < charLimit) {
      outputSentences += sentence._1
      sentencesLength += sentence._1.trim().length()
    }

    val summary = new StringBuilder()

    val rearrangedSentences = rearrangeSentences(originalSentences, outputSentences.toSeq)

    sentenceIndex = 0
    for (sentence <- rearrangedSentences) {
      if (sentenceIndex == (rearrangedSentences.length - 1)) {
        summary ++= (sentence.trim() + ".")
        sentenceIndex += 1
      }
      else {
        summary ++= (sentence.trim() + ". ")
        sentenceIndex += 1
      }
    }

    return summary.toString
  }

  /**
   * Given a String, returns the most frequent word counts
   * @param count Max number of words in set
   * @param words The String to analyze, is tokenized internally
   * @return A HashMap of the most frequent words in String along with counts
   */
  def getMostFreqWords(count : Int, str : String) : HashMap[String, Int] = {
    val tokens = LuceneTokenizer.tokenize(str)
    val wordFreq = new HashMap[String,Int]()
    for (token <- tokens) {
      wordFreq.get(token) match {
        case Some(x) => wordFreq.put(token, x + 1)
        case None => wordFreq.put(token, 1)
      }
    }

    return wordFreq
  }

  /**
   * Reorders a sub seq to be the same order as an original sequence
   * @param originalSeq The original seq of Strings
   * @param subSeq A rearranged sub seq of Strings
   * @return The sub seq in the same order as the original
   */
  def rearrangeSentences(originalSeq : Seq[String], subSeq : Seq[String]) : Seq[String] = {
    val result : LinkedHashSet[String] = new LinkedHashSet()
    for (sentence <- originalSeq){
      if(subSeq.contains(sentence)) {
        result += sentence
      }

    }
    return result.toSeq
  }
}
