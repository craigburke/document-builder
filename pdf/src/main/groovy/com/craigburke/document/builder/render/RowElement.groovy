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
        row.children.each { cell ->
            cellElements << new CellElement(cell)
        }
    }

    int getTotalHeight() {
        cellElements.max { it.totalHeight }.totalHeight
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

    boolean isFirstRow() {
        (node == node.parent.children.first())
    }

}
