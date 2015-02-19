package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Row

class RowElement {

    int startY
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
