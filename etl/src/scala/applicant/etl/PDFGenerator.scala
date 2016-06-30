package applicant.etl

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font

/** 
  * Generates a PDF file that contains the strings from the generated resume
  */

class PDFGenerator() {
    // Create a new empty document
    val document = new PDDocument()
    var nameGet = false
    var name = "" 

    // Create a new blank page and add it to the document
    val page = new PDPage()
    document.addPage(page)

    // Create a new font object selecting one of the PDF base fonts
    val font = PDType1Font.HELVETICA

    // Start a new content stream which will "hold" the to be created content
    val contentStream = new PDPageContentStream(document, page)

    contentStream.beginText();
    contentStream.setFont( font, 12 )

    //sets offset for each new line in PDF
    contentStream.setLeading(14.5f)

    //initially moves text to upper left of page
    contentStream.moveTextPositionByAmount( 50, 750 )

    /**
      * adds a line to the PDF document. each string has been trimmed to remove space characters, as 
      * PDFBox does not take their encoding. also checks to see if a string is longer than 80 characters, 
      * which then separates the string into substrings to avoid running off the page
      *
      * @param trimLine - string to add to PDF
      */

    def addLine(trimLine: String) : Unit = {
        if (trimLine.length() > 80) {
                                                   
            var count = 0
            var index = 0
            var track = 80

            while (count < trimLine.length()) {
                count = trimLine.indexOf(' ', track)
                contentStream.showText(trimLine.substring(index, count))
                contentStream.newLine()
                index = count
                count += 80
                track += 80

            }
            contentStream.showText(trimLine.substring((index + 1), trimLine.length()))
            contentStream.newLine()
        }
        
        else {
            contentStream.showText(trimLine)
            contentStream.newLine()
        }


    }

    /**
      * closes the stream to PDF and saves it to the desired directory
      *
      * @param name - first string of the PDF doc (the name of the applicant), used for naming the PDF file
      */

    def end(name: String, direc: String) : Unit = {
        contentStream.endText();

        // Make sure that the content stream is closed:
        contentStream.close();
         // Save the newly created document
        document.save(direc + "/" + name + ".pdf")

        // finally make sure that the document is properly
        // closed.
        document.close()

    }
}