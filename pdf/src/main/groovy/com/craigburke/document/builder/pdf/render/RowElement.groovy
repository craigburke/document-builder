package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Row

class RowElement {

    Row node
    List<CellElement> cellElements = []

    RowElement(Row row) {
        this.node = row
        row.cells.each { cell ->
            cellElements << new CellElement(cell)
        }
    }

    int getHeight() {
        cellElements.max { it.height }
    }

    int getWidth() {
        cellElements.add { it.node.with }
    }

    boolean isFullyRendered() {
        cellElements.every { it.fullyRendered }
    }
}
