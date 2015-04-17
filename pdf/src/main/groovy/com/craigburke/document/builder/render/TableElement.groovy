package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table

/**
 * Rendering element for a Table node
 * @author Craig Burke
 */
class TableElement implements Renderable {
    Table table
    List<RowElement> rowElements = []
    private int rowStart = 0
    private int rowsParsedCount = 0
    private boolean parsedAndRendered = false

    TableElement(Table table, PdfDocument pdfDocument, float startX) {
        this.startX = startX
        this.pdfDocument = pdfDocument

        this.table = table
        table.children.each { Row row ->
            rowElements << new RowElement(row, pdfDocument, startX)
        }
    }

    void parse(float height) {
        if (!rowElements) {
            return
        }
        if (parsedAndRendered) {
            rowStart += rowsParsedCount
            parsedAndRendered = false
        }
        rowsParsedCount = 0

        boolean reachedEnd = false
        float remainingHeight = height

        while (!reachedEnd) {
            RowElement rowElement = rowElements[rowStart + rowsParsedCount]
            rowElement.parse(remainingHeight)

            if (rowElement.fullyParsed) {
                rowsParsedCount++
            }
            else {
                reachedEnd = true
            }

            if (rowElement == rowElements.last()) {
                reachedEnd = true
            }

            remainingHeight -= rowElement.parsedHeight
        }
        parsedAndRendered = false
    }

    boolean getFullyParsed() {
        (rowElements) ? rowElements.every { it.fullyParsed } : true
    }

    float getTotalHeight() {
        rowElements*.totalHeight.sum() as float ?: 0
    }

    float getParsedHeight() {
        table.margin.top + (rowElements*.parsedHeight.sum() as float ?: 0) + (fullyParsed ? table.margin.bottom : 0)
    }

    int getRowEnd() {
        rowStart + rowsParsedCount - 1
    }

    void renderElement(float startY) {
        float rowStartY = startY
        rowElements[rowStart..rowEnd].each {
            it.render(rowStartY)
            rowStartY += it.parsedHeight
        }
        parsedAndRendered = true
    }

}
