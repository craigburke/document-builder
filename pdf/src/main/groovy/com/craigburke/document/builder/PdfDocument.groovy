package com.craigburke.document.builder

import com.craigburke.document.core.Document
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

/**
 * Document node element
 * @author Craig Burke
 */
class PdfDocument {

    float x = 0
    float y = 0

    Document document
    PDDocument pdDocument
    int pageNumber = 0

    PDPageContentStream contentStream
    List<PDPage> pages = []

    PdfDocument(Document document) {
        pdDocument = new PDDocument()
        this.document = document
        addPage()
    }

    void scrollToStartPosition() {
        x = document.margin.left
        y = document.margin.top
    }

    int getPageBottomY() {
        currentPage.mediaBox.height - document.margin.bottom
    }

    private PDRectangle getRectangle(BigDecimal width, BigDecimal height) {
        new PDRectangle(width.floatValue(), height.floatValue())
    }

    void addPage() {
        def newPage = new PDPage()
        newPage.setMediaBox(getRectangle(document.width, document.height))
        if(document.isLandscape()) {
            newPage.setRotation(90)
        }
        pages << newPage
        pageNumber++

        contentStream?.close()
        contentStream = new PDPageContentStream(pdDocument, currentPage)

        scrollToStartPosition()
        pdDocument.addPage(newPage)
    }

    PDPage getCurrentPage() {
        pages[pageNumber - 1]
    }

    void setPageNumber(int value) {
        this.pageNumber = value
        contentStream?.close()
        contentStream = new PDPageContentStream(pdDocument, currentPage, true, true)
        scrollToStartPosition()
    }

    float getTranslatedY() {
        currentPage.mediaBox.height - y
    }

    void scrollDownPage(float amount) {
        if (remainingPageHeight < amount) {
            float amountDiff = amount - remainingPageHeight
            addPage()
            y += amountDiff
        }
        else {
            y += amount
        }

    }

    float translateY(Number value) {
        currentPage.mediaBox.height - value
    }

    float getRemainingPageHeight() {
        (currentPage.mediaBox.height - document.margin.bottom) - y
    }

}
