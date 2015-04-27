package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table

/**
 * Rendering element for a Table node
 * @author Craig Burke
 */
class TableRenderer implements Renderable {
    Table table
    List<RowRenderer> rowRenderers = []
    private int rowStart = 0
    private int rowsParsedCount = 0
    private boolean parsedAndRendered = false

    TableRenderer(Table table, PdfDocument pdfDocument, float startX) {
        this.startX = startX
        this.pdfDocument = pdfDocument

        this.table = table
        table.children.each { Row row ->
            rowRenderers << new RowRenderer(row, pdfDocument, startX)
        }
    }

    void parse(float height) {
        if (!rowRenderers) {
            return
        }
        if (parsedAndRendered) {
            rowStart += rowsParsedCount
            parsedAndRendered = false
        }
        rowsParsedCount = 0

        boolean reachedEnd = false
        float remainingHeight = height - table.border.size

        while (!reachedEnd) {
            RowRenderer rowElement = rowRenderers[rowEnd]
            rowElement.parse(remainingHeight)
            if (rowElement.fullyParsed) {
                rowsParsedCount++
            }

            remainingHeight -= rowElement.parsedHeight

            if (remainingHeight < 0) {
                reachedEnd = true
            }
            else {
                if (remainingHeight == 0 || rowElement == rowRenderers.last()) {
                    reachedEnd = true
                }
            }
        }
        parsedAndRendered = false
    }

    boolean getFullyParsed() {
        (rowRenderers) ? rowRenderers.every { it.fullyParsed } : true
    }

    float getTotalHeight() {
        (rowRenderers*.totalHeight.sum() as float ?: 0) + table.border.size
    }

    float getParsedHeight() {
        (rowRenderers[rowStart..rowEnd]*.parsedHeight.sum() as float ?: 0f) + (onFirstPage ? table.border.size : 0)
    }

    int getRowEnd() {
        rowStart + (rowsParsedCount == 0 ? 0 : rowsParsedCount - 1)
    }

    void renderElement(float startY) {
        float rowStartY = startY
        rowRenderers[rowStart..rowEnd].each {
            it.render(rowStartY)
            rowStartY += it.parsedHeight
            it.cellRenderers.each { it.cell.currentRow++ }
        }
        parsedAndRendered = true
    }

}
