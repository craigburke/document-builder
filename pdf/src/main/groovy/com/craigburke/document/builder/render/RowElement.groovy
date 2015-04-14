package com.craigburke.document.builder.render

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.builder.RenderState

/**
 * Rendering element for the Row node
 * @author Craig Burke
 */
class RowElement implements Renderable {

    float startY
    float startX
    boolean spansMultiplePages = false

    Row node
    List<CellElement> cellElements = []

    RowElement(Row row, float startX) {
        this.node = row
        this.startX = startX

        Table table = row.parent
        int cellX = this.startX + table.border.size
        row.children.each { Cell cell ->
            cellElements << new CellElement(cell, cellX)
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
        cellElements*.totalHeight.max()
    }

    float getParsedHeight() {
        cellElements*.parsedHeight.max()
    }

    void render(Document document, RenderState renderState) {
        cellElements.each {
            document.element.y = startY
            it.render(document, renderState)
        }
    }

    boolean isFirstRow() {
        (node == node.parent.children.first())
    }

}
