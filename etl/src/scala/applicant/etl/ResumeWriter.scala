package applicant.etl

import java.io.{File, FileWriter}

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

/**
 * Tags, formats and writes a resume out to a FileWriter.
 */
class ResumeWriter(output: FileWriter, format: String) {
    private val separators: Array[String] = Array(" , ", " - ", " ", " – ", " | ", "\n")
    private var separatorCount: Int = 0
    private var varianceCount: Int = 0
    private var personCount: Int = 0
    private val charGenerator = new Random(1024)

    /**
     * Process and write file out to FileWriter.
     *
     * @param file input file
     */
    def write(file: File) {
        // Read entire file into Array of lines
        write(Source.fromFile(file).getLines.toSeq)
    }

    /**
     * Process and write a Sequence of Strings out to FileWriter.
     *
     * @param lines input lines
     */
    def write(lines: Seq[String]) {
        if (isNlpFormat()) {
            writePerson(lines(0))
        }

        var sectionName: String = null
        var section: ListBuffer[String] = null

        for (x <- 1 until lines.size) {
            if (lines(x).matches("(WORK EXPERIENCE|EDUCATION|SKILLS|LINKS|ADDITIONAL INFORMATION)")) {
                if (sectionName != null){
                    writeSection(sectionName, section.toSeq)
                }

                sectionName = lines(x)
                section = new ListBuffer[String]()
            }
            else if (sectionName != null) {
                section += lines(x)
            }
            else if (lines(x).matches(".+? - Email me.*")) {
                writeLocation(lines(x))
            }
            else if (isNotBlank(lines(x)) && isNlpFormat()) {
                output.write(lines(x) + "\n")
            }
        }

        // Write final section
        if (sectionName != null) {
            writeSection(sectionName, section.toSeq)
        }

        output.write("\n")
    }

    /**
     * Tags and writes a person out to the FileWriter.
     *
     * @param line input line with person info
     */
    private def writePerson(line: String) {
        personCount += 1

        // Inject name variance
        val tokens = line.split(" ")
        if (personCount % 13 == 0 && tokens.length == 2) {
            val middleInitial = getRandomChar()

            // Generate name variants with middle initial
            if (personCount % 3 == 1) {
                writeEntity("person", tokens(0) + " " + middleInitial + " " + tokens(1), false)
                output.write("\n")
            }
            else if (personCount % 3 == 2) {
                writeEntity("person", tokens(0) + " " + middleInitial + ". " + tokens(1), false)
                output.write("\n")
            }
            else {
                writeEntity("person", line.toUpperCase(), false)
                output.write("\n")
            }
        }
        else {
            // Write original name
            writeEntity("person", line, false)
            output.write("\n")
        }
    }

    /**
     * Writes the master location section if found.
     *
     * @param line current line
     */
    private def writeLocation(line: String) {
        val parts = line.split(" - ")

        writeEntity("location", parts(0), false)
        output.write(" - " + parts(1))
    }

    /**
     * Tags and writes a resume section to FileWriter.
     *
     * @param name section name
     * @param lines Seq[String] of lines representing the section
     */
    private def writeSection(name: String, lines: Seq[String]) {
        if (isNlpFormat()) {
            output.write(name + "\n")
        }

        if (name == "WORK EXPERIENCE") {
            writeExperience(lines)
        }
        else if (name == "EDUCATION") {
            if (isNlpFormat()) {
                writeEducation(lines)
            }
        }
        else if (isNlpFormat()) {
            for (x <- 0 until lines.size) {
                if (isNotBlank(lines(x))) {
                    output.write(lines(x) + "\n")
                }
            }
        }
    }

    /**
     * Tags and writes the experience section.
     *
     * @param lines Seq[String] of lines representing work experience
     */
    private def writeExperience(lines: Seq[String]) {
        var index:Int = 0

        for (x <- 0 until lines.size) {
            if (isNotBlank(lines(x))) {
                // Look for Job Title, Organization and Location. Jobs start after a newline and the 2nd line
                // has job info separated by dashes
                if (index == 0 && lines.size > x+1 && lines(x+1).contains(" - ")) {
                    if (isNlpFormat()) {
                        writeEntity("title", lines(x), true)
                    }
                    else {
                        output.write(lines(x).toUpperCase())
                    }

                    output.write("\n")
                }
                else if (index == 1 && lines(x).contains(" - ")) {
                    if (isNlpFormat()) {
                        writeOrganization(lines(x))
                    }
                }
                else {
                    var line = lines(x)
                    if (!isNlpFormat()) {
                        line = lines(x).replaceAll("(•|●|〓|➢)", "-")
                    }
                    output.write(line + "\n")
                }
                index += 1
            }
            else {
                if (index > 0 && !isNlpFormat()) {
                    output.write("\n")
                }

                index = 0
            }
        }
    }

    /**
     * Tags and writes the education section.
     *
     * @param lines Seq[String] of lines representing education
     */
    private def writeEducation(lines: Seq[String]) {
        var index:Int = 0

        for (x <- 0 until lines.size ) {
            if (isNotBlank(lines(x))) {
                if (index == 0 && lines.size > x+1 && !lines(x+1).matches(".*\\d{4}.*")) {
                    writeEntity("degree", lines(x), true)
                    output.write("\n")
                }
                else if (index == 1 && !lines(x).matches(".*\\d{4}.*")) {
                    writeSchool(lines(x))
                }
                else {
                    output.write(lines(x) + "\n")
                }
                index += 1
            }
            else {
                index = 0
            }
        }
    }

    /**
     * Tags and writes an organization.
     *
     * @param lines Seq[String] of lines representing an organization
     */
    private def writeOrganization(line: String) {
        val values = line.split(" - ")

        writeEntity("organization", values(0), true)
        if (values.length > 1){
            var start = 1
            val separator = getSeparator()

            // Skip experience that only has a date range
            if (!values(1).matches(".*\\d{4}.*")) {
                output.write(separator)
                writeEntity("location", values(1), true)
                start = 2
            }

            for (y <- start until values.size) {
                output.write(separator)
                output.write(values(y))
            }
        }

        output.write("\n")
    }

    /**
     * Tags and writes a school
     *
     * @param lines Seq[String] of lines representing a school
     */
    private def writeSchool(line: String) {
        val values = line.split(" - ")

        writeEntity("school", values(0), true)
        if (values.length > 1) {
            for (y <- 1 until values.size) {
                writeSeparator()
                output.write(values(y))
            }
        }
        output.write("\n")
    }

    /**
     * Checks if a line is not null or has non whitespace characters.
     *
     * @param line input line
     * @return Boolean
     */
    private def isNotBlank(line: String) : Boolean = {
        return line.trim().length() > 0
    }

    /**
     * Writes a tagged entity out to FileWriter.
     *
     * @param entity entity type
     * @param text entity text
     * @param variance if variance should be added to the output
     */
    private def writeEntity(entity: String, text: String, variance: Boolean) {
        varianceCount += 1

        output.write("<START:" + entity + "> ")
        output.write(text)

        // Inject data variance
        if (variance && varianceCount % 10 == 0) {
            output.write(",")
        }

        output.write(" <END>")
    }

    /**
    * Writes a separator, injecting data variance. Variance prevents algorithms from overfitting the output
    * file format.
    *
    * @return String
    */
    private def getSeparator(): String = {
        // Inject data variance
        separatorCount += 1
        return separators(separatorCount % separators.size)
    }

    /**
     * Writes a separator, injecting data variance. Variance prevents algorithms from overfitting the output
     * file format.
     */
    private def writeSeparator() {
        output.write(getSeparator())
    }

    /**
     * Generates a random character.
     *
     * @return Char
     */
    private def getRandomChar(): Char =  {
        return (charGenerator.nextInt(26) + 65).toChar
    }

    /**
     * Returns true if the output file format is 'nlp'. False otherwise.
     *
     * @return Boolean
     */
    private def isNlpFormat(): Boolean = {
        return format == "nlp"
    }
}
