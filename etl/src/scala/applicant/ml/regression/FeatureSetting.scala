package applicant.ml.regression

import scala.collection.mutable.ListBuffer

class FeatureSetting(featureName: String, isEnabled: Boolean, featureValues: ListBufer[AnyRef]) {
  val name: String = featureName
  val enabled: Boolean = isEnabled
  val values = featureValues
}
