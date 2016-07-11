package applicant.etl

import java.io.{BufferedInputStream, BufferedOutputStream, FileOutputStream, InputStream}
import java.net.{URL, URLEncoder}

import org.htmlcleaner.HtmlCleaner

import scala.collection.JavaConversions._
import scala.io.Source

import scopt.OptionParser

import org.slf4j.{Logger, LoggerFactory}

/**
 * Searches and downloads a batch of resumes.
 */
object DownloadResumes {
    private val RESUME_PREFIX = "http://www.indeed.com"
    private val URL_TEMPLATE = "http://www.indeed.com/resumes/QUERY/in-LOCATION?start=0"

    // Command line arguments
    case class Command(query: String = "", location: String = "", outputDir: String = "")

    //Logger
    val log: Logger = LoggerFactory.getLogger(getClass())

    /**
     * Builds a URLConnection and returns it's InputStream. InputStream must be closed.
     *
     * @param url connection url
     * @return InputStream
     */
    def getConnection(url: String) : InputStream = {
        val conn = new URL(url).openConnection()
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        conn.connect()

        return new BufferedInputStream(conn.getInputStream())
    }

    /**
     * Downloads url contents and stores into file.
     *
     * @param url url to download
     * @param file output file
     */
    def download(url: String, file: String) {
        log.info("Downloading " + url)
        val input = getConnection(url)
        val output = new BufferedOutputStream(new FileOutputStream(file))
        val buffer = new Array[Byte](8192)

        try {
            var bytes: Int = 0
            while ({ bytes = input.read(buffer); bytes != -1 }) {
    			output.write(buffer, 0, bytes)
    		}
        }
        finally {
            input.close()
            output.close()
        }
    }

    /**
     * Builds a formatted download url.
     *
     * @param href input href to download
     * @return String
     */
    def buildUrl(href: String) : String = {
        return RESUME_PREFIX + href.replaceAll("&#39;", "'") + "/pdf"
    }

    /**
     * Executes a search using command line arguments.
     *
     * @param options command line arguments
     */
    def search(options: Command) {
        log.info("Searching resumes for " + options.query + " - "  + options.location)

        val query = URLEncoder.encode(options.query, "UTF-8")
        val location = options.location.replace(" ", "-")
        var url = URL_TEMPLATE.replaceAll("QUERY", query).replaceAll("LOCATION", location)

        // Read entire HTML structure into memory
        val input = getConnection(url)
        val data = Source.fromInputStream(input).mkString
        input.close()

        val root = new HtmlCleaner().clean(data)
        val items = root.findElementByAttValue("class", "resultsList", true, true)

        val rows = items.getElementListByName("li", false)
        for (row <- rows) {
            val id = row.getAttributeByName("id")
            val linkNode = row.findElementByAttValue("class", "app_link", true, true)
            val href = linkNode.getAttributeByName("href").replaceAll("\\?.*", "")

            // Download PDF copy of resume
            download(buildUrl(href), options.outputDir + "/" + id + ".pdf")
        }
    }

    /**
     * Main method
     */
    def main(args: Array[String]) {
        val parser = new OptionParser[Command]("DownloadResumes") {
            opt[String]('q', "query") required() valueName("<query>") action { (x, c) =>
                c.copy(query = x)
            } text("Search Query")
            opt[String]('l', "location") required() valueName("<location>") action { (x, c) =>
                c.copy(location = x)
            } text("Location (City-State)")
            opt[String]('o', "outputDir") required() valueName("<output dir>") action { (x, c) =>
                c.copy(outputDir = x)
            } text("Output Directory")

            note("Resume Search\n")
            help("help") text("Prints this usage text")
        }

        // Parses command line arguments and passes them to the search
        parser.parse(args, Command()) match {
            case Some(options) =>
                search(options)
            case None =>
        }
    }
}
