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

    boolean isFullyParsed() {
        cellElements.every { it.fullyParsed }
    }

    float getTotalHeight() {
        cellElements.max { it.totalHeight }.totalHeight
    }

    float getParsedHeight() {
        cellElements.max { it.parsedHeight }.parsedHeight
    }

    void render(Document document, RenderState renderState) {
        cellElements*.render(document, renderState)
    }

    boolean isFirstRow() {
        (node == node.parent.children.first())
    }

}
