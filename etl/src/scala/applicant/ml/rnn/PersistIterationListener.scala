package applicant.ml.rnn

import org.deeplearning4j.nn.api.Model
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.api.IterationListener
import org.nd4j.linalg.factory.Nd4j

import org.slf4j.{Logger, LoggerFactory}

import org.apache.commons.io.FileUtils
import java.io.{DataOutputStream, File}

import java.nio.file.{Files, Paths}

/**
 * IterationListener that logs the current score and stores the current model state to file.
 */
class PersistIterationListener(json: String, coefficients: String) extends IterationListener {
    val log: Logger = LoggerFactory.getLogger(getClass())

    var isInvoked: Boolean = false
    var iterCount: Long = 0
    var bestScore: Double = -1
    var threshold: Int = 60

    /**
	 * {@inheritDoc}
	 */
    def invoked(): Boolean = {
        return isInvoked
    }

    /**
	 * {@inheritDoc}
	 */
    def invoke() {
        this.isInvoked = true
    }

    /**
	 * {@inheritDoc}
	 */
    def iterationDone(model: Model, iteration: Int) {
        invoke()

        val score: Double = model.score()
        log.info("Score at iteration " + iterCount + " is " + score)

        // Check and persist model
        save(model, score)

        iterCount += 1
    }

    /**
     * Saves a MultiLayerNetwork to file. This method will only save the model if the current
     * score is below a threshold and it's the best score this method has seend.
     *
     * @param model NN model
     * @param score current model score
     */
    def save(model: Model, score: Double) {
        if (model.isInstanceOf[MultiLayerNetwork]) {
            val net: MultiLayerNetwork = model.asInstanceOf[MultiLayerNetwork]
            if (score < threshold && (score < bestScore || bestScore == -1)) {
                log.info("Persisting model parameters to: " + coefficients)
                log.info("Persisting model configuration to: " + json)
                log.info("Model score is: " + score)
                bestScore = score

                try {
                    //Write the network parameters:
                    val dos: DataOutputStream = new DataOutputStream(Files.newOutputStream(Paths.get(coefficients)))
                    Nd4j.write(net.params(), dos)

                    //Write the network configuration:
                    FileUtils.write(new File(json), net.getLayerWiseConfigurations().toJson())
                }
                catch {
                    case ex: Exception =>
                        log.error(ex.getMessage(), ex)
                }
            }
        }
    }
}
