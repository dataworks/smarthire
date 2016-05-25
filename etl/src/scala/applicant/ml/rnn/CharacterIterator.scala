package applicant.ml.rnn

import java.io.{File, IOException}
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.{Collections, HashMap, LinkedList, List, Map, NoSuchElementException, Random}

import org.deeplearning4j.datasets.iterator.DataSetIterator
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.factory.Nd4j

import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

/**
 * DataSetIterator used to pull String minibatches from an input file.
 */
class CharacterIterator(file: String, miniBatchSize: Int, exampleLength: Int) extends DataSetIterator {
	val log: Logger = LoggerFactory.getLogger(getClass())

	val exampleStartOffsets: LinkedList[Integer] = new LinkedList();

	var charToIdxMap: Map[Character, Integer] = new HashMap()
	var validCharacters: Array[Char] = Array[Char]()
	var fileCharacters: Array[Char] = Array[Char]()
	var examplesSoFar = 0

	initValidation(file, miniBatchSize)
	init(file, miniBatchSize)

	/**
	 * Validates input parameters.
	 *
	 * @param file input file
	 * @param miniBatchSize batch size
	 */
	private def initValidation(file: String, miniBatchSize: Int) {
		if (!new File(file).exists()) {
			val msg = s"Could not access file (does not exist): $file"
      		throw new IOException(msg)
    	}

		if (miniBatchSize <= 0) {
			val msg = "Invalid miniBatchSize (must be > 0)"
			throw new IllegalArgumentException(msg)
    	}
	}

	/**
	 * Initializes the iterator.
	 *
	 * @param file input file
	 * @param miniBatchSize batch size
	 */
	private def init(file: String, miniBatchSize: Int) {
		charToIdxMap = Characters.getIndices()
		validCharacters = Characters.getCharacters()

		// Load file and convert contents to a char[]
		val newLineValid: Boolean = charToIdxMap.containsKey('\n')
		val lines = Files.readAllLines(new File(file).toPath, Charset.forName(Characters.getCharsetName())).asScala

		// Add lines.size() to account for newline characters at end of each line
		val maxSize: Int = lines.map(_.length).fold(lines.size)(_ + _ )
		fileCharacters = lines.flatMap({ s =>
			val filtered = s.filter(charToIdxMap.containsKey(_)).toString
      		if (newLineValid) filtered + "\n" else filtered
    	}).toArray

		if (exampleLength >= fileCharacters.length) {
			val msg = s"exampleLength=$exampleLength cannot exceed number of valid characters in file (${fileCharacters.length})"
			throw new IllegalArgumentException(msg)
		}

		val nRemoved = maxSize - fileCharacters.length
		val msg = s"Loaded and converted file: ${fileCharacters.length} valid characters of ${maxSize} total characters (${nRemoved}) removed"
		log.info(msg)

		//This defines the order in which parts of the file are fetched
		val nMinibatchesPerEpoch = (fileCharacters.length-1) / exampleLength - 2   //-2: for end index, and for partial example
		(0 until nMinibatchesPerEpoch).foreach { i =>
			exampleStartOffsets.add(i * exampleLength)
		}

		// Shuffle batch order to prevent bias in the ml algorithm
    	Collections.shuffle(exampleStartOffsets, Characters.getRandom())
 	}

	/**
	 * {@inheritDoc}
	 */
	def hasNext: Boolean = {
		return exampleStartOffsets.size() > 0
	}

	/**
	 * {@inheritDoc}
	 */
	def next(): DataSet = {
		return next(miniBatchSize)
	}

	/**
	 * {@inheritDoc}
	 */
  	def next(num: Int): DataSet = {
    	if (exampleStartOffsets.size() == 0) {
			throw new NoSuchElementException()
		}

		val currMinibatchSize = Math.min(num, exampleStartOffsets.size())

		// Note the order here:
    	// dimension 0 = number of examples in minibatch
    	// dimension 1 = size of each vector (i.e., number of characters)
    	// dimension 2 = length of each time series/example
		val input = Nd4j.zeros(currMinibatchSize, validCharacters.length, exampleLength)
		val labels = Nd4j.zeros(currMinibatchSize, validCharacters.length, exampleLength)

		(0 until currMinibatchSize).foreach { i =>
			val startIdx = exampleStartOffsets.removeFirst()
			val endIdx = startIdx + exampleLength
			var currCharIdx = charToIdxMap.get(fileCharacters(startIdx))	//Current input
			var c = 0
			(startIdx + 1 until endIdx).foreach { j =>
				val nextCharIdx = charToIdxMap.get(fileCharacters(j))		//Next character to predict
				input.putScalar(Array[Int](i, currCharIdx, c), 1.0)
				labels.putScalar(Array[Int](i, nextCharIdx, c), 1.0)
				currCharIdx = nextCharIdx
				c += 1
			}
		}

		return new DataSet(input, labels)
	}

	/**
	 * {@inheritDoc}
	 */
	def totalExamples(): Int = {
 		return (fileCharacters.length - 1) / miniBatchSize - 2
	}

	/**
	 * {@inheritDoc}
	 */
	def inputColumns(): Int = {
		return validCharacters.length
	}

	/**
	 * {@inheritDoc}
	 */
	def totalOutcomes(): Int = {
		return validCharacters.length
	}

	/**
	 * {@inheritDoc}
	 */
	def reset() {
		exampleStartOffsets.clear()
		val nMinibatchesPerEpoch = totalExamples()
		(0 until nMinibatchesPerEpoch).foreach { i =>
			exampleStartOffsets.add(i * miniBatchSize)
		}

		// Shuffle batch order to prevent bias in the ml algorithm
		Collections.shuffle(exampleStartOffsets, Characters.getRandom())
	}

	/**
	 * {@inheritDoc}
	 */
	def batch(): Int = {
		return miniBatchSize
	}

	/**
	 * {@inheritDoc}
	 */
 	def cursor(): Int = {
		return totalExamples() - exampleStartOffsets.size()
	}

	/**
	 * {@inheritDoc}
	 */
	def numExamples(): Int = {
		return totalExamples()
	}

	/**
	 * {@inheritDoc}
	 */
	def setPreProcessor(preProcessor: DataSetPreProcessor) {
		throw new UnsupportedOperationException("Not implemented")
	}

	/**
	 * {@inheritDoc}
	 */
  	def getLabels: List[String] = {
		throw new UnsupportedOperationException("Not implemented")
	}

	/**
	 * {@inheritDoc}
	 */
	def remove() {
		throw new UnsupportedOperationException("Not implemented")
	}
}
