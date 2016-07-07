package applicant.ml

import org.apache.commons.io.FileUtils
import java.io._

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

  /**
   * Will remove the folder specified and all subdirectories and files
   *
   * @param path A path to a folder where the model is stored
   */
  def clearCustomModelPath(path: String) {
    checkDeletePath(new File(path))
  }

  /**
   * Will serialize an obeject into a byte array. Used to store model data that does not have a save function.
   *
   * @param o An object that needs to be serialized
   * @return A byte array of the object
   */
  def serialize[A](o: A): Array[Byte] = {
    val ba = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(ba)
    out.writeObject(o)
    out.close()
    return ba.toByteArray()
  }

  /**
   * Will deserialize a byte array and convert it back into its original object
   *
   * @param buffer The byte array of the serialized object
   * @return The deserialized object
   */
  def deserialize[A](buffer: Array[Byte]): A = {
    val in = new ObjectInputStream(new ByteArrayInputStream(buffer))
    return in.readObject().asInstanceOf[A]
  }
}
