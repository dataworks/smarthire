package applicant.ml.rnn

import java.io.{DataInputStream, File, FileInputStream}

import org.apache.commons.io.FileUtils
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.ops.transforms.Transforms

/**
 * TextNet is a RNN that generates characters based on using a MultiLayerNetwork.
 */
class TextNet(net: MultiLayerNetwork) {
    /**
     * Creates a new TextNet using a previously build model.
     *
     * @param json path to json configuration
     * @param coefficients path to model coefficients
     */
    def this(json: String, coefficients: String) {
        this(TextNet.readNetwork(json, coefficients))
    }

    /**
     * Retrieves an Array[String] from the network.
     *
     * @param initialization starting text for the String
     * @param charactersToSample size of the output String
     * @param numSamples number of Strings to generate
     * @return Array[String]
     */
    def getText(initialization: String, charactersToSample: Int, numSamples: Int): Array[String] = {
        // Set up initialization. If no initialization: use a random character
        val initStr: String = if (initialization == null) {
            String.valueOf(Characters.getRandomCharacter())
        }
        else {
            initialization
        }

        // Create input for initialization
        val initializationInput: INDArray = Nd4j.zeros(numSamples, Characters.size(), initStr.length())
        val init: Array[Char] = initStr.toCharArray
        init.indices.foreach { i =>
            val idx = Characters.getIndex(initStr(i))
            (0 until numSamples).foreach { j =>
                initializationInput.putScalar(Array[Int](j, idx, i), 1.0f)
            }
        }

        val sb = Array.fill(numSamples)(new StringBuilder(initStr))

        // Sample from network (and feed samples back into input) one character at a time (for all samples)
        // Sampling is done in parallel here
        net.rnnClearPreviousState()
        var output: INDArray = net.rnnTimeStep(initializationInput)
        output = output.tensorAlongDimension(output.size(2)-1, 1, 0)  //Gets the last time step output

        (0 until charactersToSample).foreach { i =>
            // Set up next input (single time step) by sampling from previous output
            val nextInput: INDArray  = Nd4j.zeros(numSamples, Characters.size())

            // Output is a probability distribution. Sample from this for each example we want to generate, and add it to the new input
            (0 until numSamples).foreach { s =>
                val outputProbDistribution = (0 until Characters.size()).map(output.getDouble(s, _)).toArray
                val sampledCharacterIdx: Int = sampleFromDistribution(outputProbDistribution)

                // Prepare next time step input
                // Add sampled character to StringBuilder (human readable output)
                nextInput.putScalar(Array[Int](s, sampledCharacterIdx), 1.0f)
                sb(s).append(Characters.getCharacter(sampledCharacterIdx))
            }

            // Do one time step of forward pass
            output = net.rnnTimeStep(nextInput)
        }

        return sb.map(_.toString)
    }

    /**
     * Gets a sample character index from an input probability distribution.
     *
     * @param distribution probability distribution
     * @return Int
     */
    private def sampleFromDistribution(distribution: Array[Double]): Int = {
        val array: Array[Double] = adjustCreativity(distribution, 0.7)

        val d = Characters.getRandom().nextDouble()
        val accum = array.scanLeft(0.0)(_ + _).tail
        val exceeds = accum.zipWithIndex.filter(_._1 >= d)

        if (exceeds.length <= 0) {
            // Should never happen if distribution is a valid probability distribution
            throw new IllegalArgumentException(s"Distribution is invalid? d=$d, accum=$accum")
        }

        return exceeds.head._2
    }

    /**
     * Adjusts the "creativity" of the probability distribution. This method converts the probabilities to log
     * probabilities which adjusts higher probabilities higher and lower ones lower. This will in turn have the model
     * more likely to pick "safe" guesses when creativity is low.
     *
     * @param distribution input probability distribution
     * @param factor creativity factor
     * @return Array[Double] adjusted probability distribution
     */
    private def adjustCreativity(distribution: Array[Double], factor: Double): Array[Double] = {
        var array: INDArray = Nd4j.create(distribution);

        // a = log(a) / scale factor
        // a = exp(a) / sum(exp(a))
        array = Transforms.log(array).div(factor);
        array = Transforms.exp(array);
        array = array.div(array.sumNumber());

        // Copy data to a new Array[Double]
        val ret: Array[Double] = new Array[Double](array.length())
        (0 until array.length()).foreach { i =>
            ret(i) = array.getDouble(i)
        }

        return ret;
    }
}

/**
 * Static TextNet methods
 */
object TextNet {
    /**
     * Builds a MultiLayerNetwork from input files.
     *
     * @param json path to json configuration
     * @param coefficients path to model coefficients
     */
    def readNetwork(json: String, coefficients: String): MultiLayerNetwork = {
        // Load network configuration from disk
        val conf: MultiLayerConfiguration = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(json)))

        // Load parameters from disk
        val input = new FileInputStream(coefficients)
        try {
            val dis: DataInputStream = new DataInputStream(input)
            val newParams: INDArray = Nd4j.read(dis)

            // Create a MultiLayerNetwork from the saved configuration and parameters
            val savedNetwork: MultiLayerNetwork = new MultiLayerNetwork(conf)
            savedNetwork.init()
            savedNetwork.setParameters(newParams)

            return savedNetwork
        }
        finally {
            input.close()
        }

        return null
    }
}
