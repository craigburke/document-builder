package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Table
import com.craigburke.document.core.TextBlock

/**
 * Rendering element for the cell node
 * @author Craig Burke
 */
class CellRenderer implements Renderable {

    float currentRowHeight = 0

    Cell cell
    List<Renderable> childRenderers = []

    CellRenderer(Cell cell, PdfDocument pdfDocument, float startX) {
        this.cell = cell
        this.startX = startX
        this.pdfDocument = pdfDocument

        Table table = cell.parent.parent
        int renderWidth = cell.width - (table.padding * 2)
        float childStartX = startX + table.padding
        cell.children.each { child ->
            if (child instanceof TextBlock) {
                childRenderers << new ParagraphRenderer(child, pdfDocument, childStartX, renderWidth)
            }
            else if (child instanceof Table) {
                childRenderers << new TableRenderer(child, pdfDocument, childStartX)
            }
        }
    }

    float getRowspanHeight() {
        cell.rowspanHeight + currentRowHeight
    }

    private float getPadding() {
        cell.parent.parent.padding
    }

    boolean getFullyParsed() {
        if (cell.rowspan > 1 && !onLastRowspanRow) {
            return true
        }
        childRenderers.every { it.fullyParsed }
    }

    float getTotalHeight() {
        (childRenderers*.totalHeight.sum() ?: 0f) + (padding * 2)
    }

    float getParsedHeight() {
        if (!onLastRowspanRow) {
            return 0
        }
        float parsedHeight = (childRenderers*.parsedHeight.sum() ?: 0f) as float
        if (onFirstPage) {
            parsedHeight += padding
        }
        if (fullyParsed) {
            parsedHeight += padding
        }
        if (cell.rowspan > 1) {
            parsedHeight -= rowspanHeight
        }
        parsedHeight
    }

    void renderElement(float startY) {
        float childY = startY
        if (cell.rowspan > 1) {
            childY -= rowspanHeight
            cell.rowspanHeight += currentRowHeight
            currentRowHeight = 0
        }
        if (onFirstPage) {
            childY += padding
        }
        childRenderers*.render(childY)
    }

    void parse(float height) {
        if (height < 0) {
            return
        }
        float parseHeight = height - padding
        childRenderers*.parse(parseHeight)
    }

    boolean isOnLastRowspanRow() {
        (cell.rowspan == 1) || (cell.rowsSpanned == (cell.rowspan - 1))
    }

}

