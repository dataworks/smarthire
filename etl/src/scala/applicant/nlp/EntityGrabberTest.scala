package applicant.nlp

import java.io._
import java.nio.charset.StandardCharsets

object EntityGrabberTest {

  def main(args: Array[String]) {

    //Read a resume into a string of lines
    var filePath: String = "test/scala/applicant/nlp/resume.txt"

    var resumeString: String = ""
    var br: BufferedReader = new BufferedReader(new FileReader(filePath))
    var line: String = ""
    while ({line = br.readLine(); line != null}) {
      resumeString += (line + "\n")
    }

    br.close()

    //Load all of the models into the EntityGrabber and test the time
    var startTime: Long = System.currentTimeMillis();

    val models = List[String]("model/nlp/en-ner-degree.bin", "model/nlp/en-ner-location.bin", "model/nlp/en-ner-organization.bin", "model/nlp/en-ner-person.bin", "model/nlp/en-ner-school.bin", "model/nlp/en-ner-title.bin")
    val patterns = "model/nlp/regex.txt"
    val grabber = new EntityGrabber(models, patterns)

    var endTime: Long = System.currentTimeMillis();
    var totalTime: Long = endTime - startTime;
    println("Entity grabber took (" + totalTime/1000 + ") seconds to load the models");

    //Store how long it took for later
    var entityTotalTime: Long = totalTime/1000

    //Test how long EntityGrabber takes to parse the lines
    startTime = System.currentTimeMillis();

    grabber.extractEntities(resumeString)

    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    println("Entity grabber took (" + totalTime/1000 + ") seconds to tokenize the resume text");

    entityTotalTime += totalTime/1000

    println("EntityGrabber took (" + entityTotalTime + ") seconds to do everything")
  }
}
