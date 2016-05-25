package applicant.nlp

import java.io.{BufferedOutputStream, File, FileOutputStream, InputStream}
import java.nio.charset.Charset
import java.util.Collections

import opennlp.tools.namefind.{NameFinderME, NameSampleDataStream, NameSampleTypeFilter, TokenNameFinderFactory,
    TokenNameFinderModel}
import opennlp.tools.util.{PlainTextByLineStream, TrainingParameters}

/**
 * Trains an Entity based OpenNLP model.
 */
class EntityModel {
    private var model: TokenNameFinderModel = null

    /**
     * Trains the model.
     *
     * @param training input stream of training data
     * @param entity entity type to filter training on
     * @param cutoff number of times a token needs to be labeled before being considered entity type
     */
    def train(training: InputStream, entity: String, cutoff: Int) {
        // Tokenize training input stream
        val lineStream = new PlainTextByLineStream(training, Charset.forName("UTF-8"))
        val sampleStream = new NameSampleTypeFilter(Array(entity), new NameSampleDataStream(lineStream))

        val trainingParams = TrainingParameters.defaultParams()
        trainingParams.put(TrainingParameters.CUTOFF_PARAM, cutoff.toString())

        // Train model
        try {
            model = NameFinderME.train("en", entity, sampleStream, trainingParams, new TokenNameFinderFactory())
        }
        finally {
            sampleStream.close()
        }
    }

    /**
     * Writes the model to file.
     *
     * @param file output file
     */
    def write(file: File) {
        if (model != null) {
            // Write out model
            var modelOut: BufferedOutputStream = null
            try {
                modelOut = new BufferedOutputStream(new FileOutputStream(file))
                model.serialize(modelOut)
            }
            finally {
                if (modelOut != null) {
                    modelOut.close()
                }
            }
        }
    }
}
