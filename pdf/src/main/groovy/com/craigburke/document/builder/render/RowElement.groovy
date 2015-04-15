package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table

/**
 * Rendering element for the Row node
 * @author Craig Burke
 */
class RowElement implements Renderable {

    Row row
    boolean spansMultiplePages = false
    List<CellElement> cellElements = []

    RowElement(Row row, PdfDocument pdfDocument, float startX, float startY) {
        this.row = row
        this.startX = startX
        this.startY = startY
        this.pdfDocument = pdfDocument

        Table table = row.parent
        float cellX = startX + table.border.size
        row.children.each { Cell cell ->
            cellElements << new CellElement(cell, pdfDocument, cellX, startY)
            cellX += cell.width + table.border.size
        }
    }

    void parseUntilHeight(float height) {
        cellElements*.parseUntilHeight(height)
    }

    boolean getFullyParsed() {
        cellElements.every { it.fullyParsed }
    }

    float getTotalHeight() {
        cellElements.sum { it.totalHeight }
    }

    float getParsedHeight() {
        cellElements.sum { it.parsedHeight }
    }

    void render() {
        cellElements*.render()
    }

    boolean isFirstRow() {
        (row == row.parent.children.first())
    }

}
