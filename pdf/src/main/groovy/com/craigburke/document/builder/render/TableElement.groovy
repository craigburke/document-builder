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
    private int rowEnd = 0

    TableElement(Table table, PdfDocument pdfDocument, float startX) {
        this.startX = startX
        this.pdfDocument = pdfDocument

        this.table = table
        table.children.each { Row row ->
            rowElements << new RowElement(row, pdfDocument, startX)
        }
    }

    void parseUntilHeight(float height) {
        if (!rowElements) {
            return
        }
        rowStart = rowEnd
        boolean reachedEnd = false
        float remainingHeight = height

        while (!reachedEnd) {
            RowElement rowElement = rowElements[rowEnd]
            rowElement.parseUntilHeight(remainingHeight)

            if (rowElement == rowElements.last()) {
                reachedEnd = true
            }
            if (!rowElement.fullyParsed) {
                reachedEnd = true
            }
            else if (rowEnd != rowElements.size() - 1) {
                rowEnd++
            }

            remainingHeight -= rowElement.parsedHeight
        }
    }

    boolean getFullyParsed() {
        (rowElements) ? rowElements.every { it.fullyParsed } : true
    }

    float getTotalHeight() {
        rowElements*.totalHeight.sum() as float ?: 0
    }

    float getParsedHeight() {
        rowElements*.parsedHeight.sum() as float ?: 0
    }

    void renderElement(float startY) {
        float rowStartY = startY + table.border.size
        rowElements[rowStart..rowEnd].each {
            it.render(rowStartY)
            rowStartY += it.parsedHeight + table.border.size
        }
    }

}
