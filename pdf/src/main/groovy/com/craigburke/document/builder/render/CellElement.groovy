package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Table
import com.craigburke.document.core.TextBlock

/**
 * Rendering element for the Cell node
 * @author Craig Burke
 */
class CellElement implements Renderable {
    Cell cell
    List<Renderable> childElements = []

    CellElement(Cell cell, PdfDocument pdfDocument, float startX) {
        this.cell = cell
        this.startX = startX
        this.pdfDocument = pdfDocument

        Table table = cell.parent.parent
        int renderWidth = cell.width - (table.padding * 2)
        float childStartX = startX + table.padding
        cell.children.each { child ->
            if (child instanceof TextBlock) {
                childElements << new ParagraphElement(child, pdfDocument, childStartX, renderWidth)
            }
            else if (child instanceof Table) {
                childElements << new TableElement(child, pdfDocument, childStartX)
            }
        }
    }

    boolean getFullyParsed() {
        childElements.every { it.fullyParsed }
    }

    float getTotalHeight() {
        float padding = cell.parent.parent.padding
        float contentHeight = (childElements*.totalHeight.sum() ?: 0f) as float
        contentHeight + padding * 2
    }

    float getParsedHeight() {
        float padding = cell.parent.parent.padding
        float contentHeight = (childElements*.parsedHeight.sum() ?: 0f) as float
        contentHeight + padding + (fullyParsed ? padding : 0f)
    }

    void renderElement(float startY) {
        pdfDocument.x = startX
        float childY = startY + cell.parent.parent.padding
        childElements[0].render(childY)
    }

    void parseUntilHeight(float height) {
        childElements*.parseUntilHeight(height)
    }
}

