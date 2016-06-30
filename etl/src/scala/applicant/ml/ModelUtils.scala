package applicant.ml

import org.apache.commons.io.FileUtils
import java.io.File

/**
 * ModelUtils is a general class that deals with checking save locations of various
 *  machine learning models
 */
object ModelUtils {
  /**
   * Will check if a path exists and delete it if so
   *
   * @param path The path to the directory that is to be deleted
   */
  def checkDeletePath(path: File) {
    if (path.exists()) {
      FileUtils.forceDelete(path);
    }
  }

  /**
   * Will remove the data and metadata folders at path/data and path/metadata if they exists
   *
   * @param path A path to the metadata folders
   */
  def clearModelPath(path: String) {
    val dataPath: File = new File(path + "/data")
    checkDeletePath(dataPath)

    val metadataPath: File = new File(path + "/metadata")
    checkDeletePath(metadataPath)
  }
}
