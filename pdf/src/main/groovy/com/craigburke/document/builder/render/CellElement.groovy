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

    CellElement(Cell cell, PdfDocument pdfDocument, float startX, float startY) {
        this.cell = cell
        this.startX = startX
        this.startY = startY
        this.pdfDocument = pdfDocument

        Table table = cell.parent.parent
        int renderWidth = cell.width - (table.padding * 2)

        cell.children.each { child ->
            if (child instanceof TextBlock) {
                childElements << new ParagraphElement(child, pdfDocument, startX, startY, renderWidth)
            }
            else if (child instanceof Table) {
                childElements << new TableElement(child, pdfDocument, startX, startY)
            }
        }
    }

    boolean getFullyParsed() {
        childElements.every { it.fullyParsed }
    }

    float getTotalHeight() {
        childElements*.totalHeight.max()
    }

    float getParsedHeight() {
        childElements*.parsedHeight.max()
    }

    void render() {
        pdfDocument.x = startX
        pdfDocument.y = startY
        childElements*.render()
    }

    void parseUntilHeight(float height) {
        childElements*.parseUntilHeight(height)
    }
}

