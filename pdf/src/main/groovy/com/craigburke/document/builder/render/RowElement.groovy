package com.craigburke.document.builder.render

import com.craigburke.document.core.Row

/**
 * Rendering element for the Row node
 * @author Craig Burke
 */
class RowElement {

    int startY
    boolean spansMultiplePages = false

    Row node
    List<CellElement> cellElements = []

    RowElement(Row row) {
        this.node = row
        row.cells.each { cell ->
            cellElements << new CellElement(cell)
        }
    }

    int getWidth() {
        cellElements.add { it.node.with }
    }

    int getRenderedHeight() {
        cellElements.max { it.renderedHeight }.renderedHeight
    }

    void setRenderedHeight(int height) {
        cellElements.each { it.renderedHeight = height }
    }

    boolean isFullyRendered() {
        cellElements.every { it.fullyRendered }
    }
}
