package applicant.ml.rnn

import java.io.{File, IOException}
import java.nio.charset.Charset

import org.apache.commons.io.FileUtils
import org.deeplearning4j.nn.api.{Layer, OptimizationAlgorithm}
import org.deeplearning4j.nn.conf.layers.{GravesLSTM, RnnOutputLayer}
import org.deeplearning4j.nn.conf.{BackpropType, MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.nd4j.linalg.ops.transforms.Transforms

import org.slf4j.{Logger, LoggerFactory}

import scopt.OptionParser

/**
 * Trains a TextNet RNN.
 */
object TextNetTrain {
    val log: Logger = LoggerFactory.getLogger(getClass())

    // Command line arguments
    case class Command(inputFile: String = "", json: String = "", coefficients: String = "")

    /**
     * Builds a MultiLayerConfiguration.
     *
     * @param lstmLayerSize number of nodes in the network
     * @param tbpttLength forward and backwards propogation - network uses history to make better guesses on the next Char
     * @param iter CharacterIterator
     * @return MultiLayerConfiguration
     */
    def buildConf(lstmLayerSize: Int, tbpttLength: Int, iter: CharacterIterator): MultiLayerConfiguration = {
        val nOut: Int = iter.totalOutcomes()

        //Set up network configuration:
        val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
            .learningRate(0.1)
            .rmsDecay(0.95)
            .seed(12345)
            .regularization(true)
            .l2(0.001)
            .weightInit(WeightInit.XAVIER)
            .updater(Updater.RMSPROP)
            .list(0)
            .layer(0, new GravesLSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                .activation("tanh").build())
            .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                .activation("tanh").build())
            .layer(2, new RnnOutputLayer.Builder(LossFunction.MCXENT).activation("softmax")
                .nIn(lstmLayerSize).nOut(nOut).build())
            .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
            .pretrain(false).backprop(true)
            .build()

        return conf
    }

    /**
     * Trains the network using input command line parameters.
     *
     * @param options command line options
     */
    def train(options: Command) = {
        //Number of units in each GravesLSTM layer
        val lstmLayerSize = 200

        //Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters
        val tbpttLength = 50

        //Size of mini batch to use when training
        val miniBatchSize = 32

        //Length of each training example sequence to use. This could certainly be increased
        val exampleLength = 1000

        //Total number of training epochs
        val numEpochs = 1

        //How frequently to generate samples from the network? 1000 characters / 50 tbptt length: 20 parameter updates per minibatch
        val generateSamplesEveryNMinibatches = 10

        //Number of samples to generate after each training epoch
        val nSamplesToGenerate = 4

        //Length of each sample to generate
        val nCharactersToSample = 300

        //Optional character initialization; a random character is used if null
        val generationInitialization: String = null

        //Get a DataSetIterator that handles vectorization of text into something we can use to train our GravesLSTM network.
        val iter: CharacterIterator = Characters.getIterator(options.inputFile, miniBatchSize, exampleLength)

        val net = new MultiLayerNetwork(buildConf(lstmLayerSize, tbpttLength, iter))
        net.init()
        net.setListeners(new PersistIterationListener(options.json, options.coefficients))

        val textNet: TextNet = new TextNet(net)

        //Print the  number of parameters in the network (and for each layer)
        val layers: Array[Layer] = net.getLayers
        val totalNumParams = layers.zipWithIndex.map({ case (layer, i) =>
            val nParams: Int = layer.numParams()
            log.info("Number of parameters in layer " + i + ": " + nParams)
            nParams
        }).sum

        log.info("Total number of network parameters: " + totalNumParams)

        var miniBatchNumber = 0

        //Do training, and then generate and print samples from network
        (0 until numEpochs).foreach { i =>
            while(iter.hasNext()) {
                val ds = iter.next()
                net.fit(ds)
                miniBatchNumber += 1

                if (miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
                    log.info("--------------------")
                    log.info("Completed " + miniBatchNumber + " minibatches of size " + miniBatchSize +
                            "x" + exampleLength + " characters" )
                    log.info("Sampling characters from network given initialization \"" +
                         (if (generationInitialization == null) "" else generationInitialization) + "\"")
                    val samples = textNet.getText(generationInitialization, nCharactersToSample, nSamplesToGenerate)
                    samples.indices.foreach { j =>
                        log.info("----- Sample " + j + " -----")
                        log.info(samples(j))
                        log.info(" ")
                    }
                }
            }

            // Reset iterator for another epoch
            iter.reset()
        }

        log.info("Training complete")
    }

    /**
     * Main method
     */
    def main(args: Array[String]) {
        val parser = new OptionParser[Command]("TextNetTrain") {
            opt[String]('i', "inputFile") required() valueName("<input file>") action { (x, c) =>
                c.copy(inputFile = x)
            } text("Training input file")
            opt[String]('j', "json") required() valueName("<json>") action { (x, c) =>
                c.copy(json = x)
            } text("RNN Model JSON Configuration")
            opt[String]('c', "coefficients") required() valueName("<coefficients>") action { (x, c) =>
                c.copy(coefficients = x)
            } text("RNN Model Coefficients")

            note("Builds a Char RNN over input text. This network can be used to generate unique text blocks.\n")
            help("help") text("Prints this usage text")
        }

        // Parses command line arguments and passes them to the search
        parser.parse(args, Command()) match {
            case Some(options) =>
                train(options)
            case None =>
        }
    }
}
