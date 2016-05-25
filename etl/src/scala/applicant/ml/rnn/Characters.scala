package applicant.ml.rnn

import java.io.{File, IOException}
import java.util.{HashMap, Map, Random}

import org.slf4j.{Logger, LoggerFactory}

/**
 * Default CharacterIterator configuration.
 */
object Characters {
	val log: Logger = LoggerFactory.getLogger(getClass())

	val random = new Random(12345)
	val characterSet: Array[Char] = getCharacterSet()
	val indexMap: Map[Character, Integer] = getIndexMap()

	/**
	 * Builds a new CharacterSet iterator.
	 *
	 * @param file input file
	 * @param miniBatchSize batch size
	 */
	def getIterator(file: String, miniBatchSize: Int, sequenceLength: Int): CharacterIterator = {
        val f = new File(file)
        log.info("Using input file: " + f.getAbsolutePath())

        if (!f.exists()) {
            throw new IOException("File does not exist: " + file)
        }

        return new CharacterIterator(file, miniBatchSize, sequenceLength)
    }

	/**
	 * Returns the default Random number generator.
	 *
	 * @return Random
	 */
	def getRandom(): Random = {
		return random
	}

	/**
	 * Returns the default character set.
	 *
	 * @return String
	 */
	def getCharsetName(): String = {
		return "UTF-8"
	}

	/**
	 * Returns an Array of valid characters.
	 *
	 * @return Array[Char]
	 */
	def getCharacters(): Array[Char] = {
		return characterSet
	}

	/**
	 * Retrieves a character at index.
	 *
	 * @param index position to return
	 * @return Char
	 */
	def getCharacter(index: Int): Char = {
		return characterSet(index)
	}

	/**
	 * Retrieves a random character from the valid character set.
	 *
	 * @return Char
	 */
	def getRandomCharacter(): Char = {
		return characterSet((random.nextDouble() * characterSet.length).toInt)
	}

	/**
	 * Returns a Map of Character to positions.
	 *
	 * @return Map[Character, Integer]
	 */
	def getIndices(): Map[Character, Integer] = {
		return indexMap
	}

	/**
	 * Retrieves a index for input character.
	 *
	 * @param char Character
	 * @return Integer
	 */
	def getIndex(char: Char): Int = {
		return indexMap.get(char)
	}

	/**
	 * Returns the number of valid characters.
	 *
	 * @return Int
	 */
	def size(): Int = {
		return characterSet.length
	}

	/**
	 * Builds an Array of valid characters.
	 *
	 * @return Array[Char]
	 */
	private def getCharacterSet(): Array[Char] = {
		return (('a' to 'z').toSeq ++
			('A' to 'Z').toSeq ++
			('0' to '9').toSeq ++
			Seq[Char]('!', '&', '(', ')', '?', '-', '\'', '"', ',', '.', ':', ';', ' ', '\n', '\t')).toArray
	}

	/**
	 * Builds a Map of Character to positions.
	 *
	 * @return Map[Character, Integer]
	 */
	private def getIndexMap(): Map[Character, Integer] = {
		val map: Map[Character, Integer] = new HashMap()

		//Store valid characters is a map for later use in vectorization
		characterSet.zipWithIndex.foreach {
			case (ch, i) => map.put(ch, i)
		}

		return map
	}
}
