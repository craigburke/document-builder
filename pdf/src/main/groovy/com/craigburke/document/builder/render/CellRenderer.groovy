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
        childRenderers.every { it.fullyParsed }
    }

    float getTotalHeight() {
        (childRenderers*.totalHeight.sum() ?: 0f) + (padding * 2)
    }

    float getParsedHeight() {
        if (!rowspanEnd) {
            return 0
        }
        float parsedHeight = (childRenderers*.parsedHeight.sum() ?: 0f) as float
        if (onFirstPage) {
            parsedHeight += padding
        }
        if (fullyParsed) {
            parsedHeight += padding
        }
        Math.max(0, parsedHeight - rowspanHeight)
    }

    void updateRowspanHeight() {
        cell.rowspanHeight += currentRowHeight
        currentRowHeight = 0
    }

    void renderElement(float startY) {
        float childY = startY
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

    boolean isRowspanEnd() {
        cell.rowspan == cell.rowspanPosition
    }

}

