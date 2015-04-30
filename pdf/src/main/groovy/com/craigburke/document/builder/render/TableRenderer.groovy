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

    private int parseStart = 0
    private int parseEnd = 0

    private boolean parsedAndRendered = false

    TableRenderer(Table table, PdfDocument pdfDocument, float startX) {
        this.startX = startX
        this.pdfDocument = pdfDocument

        this.table = table
        table.children.each { Row row ->
            rowRenderers << new RowRenderer(row, pdfDocument, startX)
        }
    }

    int getParseStart() {
        this.parseStart
    }

    int getParseEnd() {
        this.parseEnd
    }

    void parse(float height) {
        if (!rowRenderers) {
            return
        }

        if (parsedAndRendered) {
            parseStart = parseEnd
        }
        else {
            parseEnd = parseStart
        }

        boolean reachedEnd = false
        float remainingHeight = height - (onFirstPage ? table.border.size : 0)

        while (!reachedEnd) {
            RowRenderer currentRenderer = rowRenderers[parseEnd]
            currentRenderer.parse(remainingHeight)
            remainingHeight -= currentRenderer.parsedHeight

            if (remainingHeight < 0) {
                parseEnd = Math.max(0, parseEnd - 1)
                reachedEnd = true
            }
            else if (remainingHeight == 0) {
                reachedEnd = true
            }
            else if (currentRenderer == rowRenderers.last()) {
                reachedEnd = true
            }

            if (!reachedEnd && currentRenderer.fullyParsed) {
                parseEnd++
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
        (rowRenderers[parseStart..parseEnd]*.parsedHeight.sum() as float ?: 0f) + (onFirstPage ? table.border.size : 0)
    }

    void renderElement(float startY) {
        float rowStartY = startY
        rowRenderers[parseStart..parseEnd].each {
            it.render(rowStartY)
            rowStartY += it.parsedHeight
            if (fullyParsed) {
                it.cellRenderers.each { it.cell.rowsSpanned++ }
            }
        }
        parsedAndRendered = true
    }

}
