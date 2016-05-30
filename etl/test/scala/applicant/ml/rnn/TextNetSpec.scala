package applicant.ml.rnn

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._
import org.scalatest.PrivateMethodTester._

import scala.math.BigDecimal

/**
 * Scala Test Spec to test the TextNet object.
 */
class TextNetSpec extends FlatSpec {
    "adjustCreativity" must "adjust creativity non-linearly" in {
        val textnet = new TextNet(null)

        val adjustCreativity = PrivateMethod[Array[Double]]('adjustCreativity)

        var results = textnet invokePrivate adjustCreativity(Array[Double](0.7, 0.2, 0.1), 1.0)
        results = results.map(x => BigDecimal(x).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
        results must equal (Array[Double](0.7, 0.2, 0.11))

        results = textnet invokePrivate adjustCreativity(Array[Double](0.7, 0.2, 0.1), 0.7)
        results = results.map(x => BigDecimal(x).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
        results must equal (Array[Double](0.81, 0.14, 0.05))

        results = textnet invokePrivate adjustCreativity(Array[Double](0.7, 0.2, 0.1), 0.3)
        results = results.map(x => BigDecimal(x).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
        results must equal (Array[Double](0.98, 0.02, 0.0))
    }
}
