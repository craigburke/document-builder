package com.craigburke.document.builder.pdfbox

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

class PdfDocument {
    int x = 0
    int y = 0

    PDDocument document
    PDPage currentPage
    PDPageContentStream contentStream

    void addPage() {
        currentPage = new PDPage()
        document.addPage(currentPage)

        contentStream?.close()
        contentStream = new PDPageContentStream(document, currentPage)
    }

    int getTranslatedY() {
        currentPage.mediaBox.height - y
    }

}
