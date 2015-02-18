package com.craigburke.document.builder.pdf

import com.craigburke.document.core.Document
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

class PdfDocument {
    int x = 0
    int y = 0

    Document document
    PDDocument pdDocument
    PDPage currentPage
    PDPageContentStream contentStream

    PdfDocument(Document document) {
        pdDocument = new PDDocument()
        this.document = document
        addPage()
    }

    void addPage() {
        x = document.margin.top
        y = document.margin.left

        currentPage = new PDPage()
        pdDocument.addPage(currentPage)

        contentStream?.close()
        contentStream = new PDPageContentStream(pdDocument, currentPage)
    }

    int getTranslatedY() {
        currentPage.mediaBox.height - y
    }

    int translateY(int value) {
        currentPage.mediaBox.height - value
    }

    int getRemainingPageHeight() {
        currentPage.mediaBox.height - y - document.margin.top - document.margin.bottom
    }

}
