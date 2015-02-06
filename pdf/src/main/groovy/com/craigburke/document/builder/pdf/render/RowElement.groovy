package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Row

class RowElement {

    Row node
    List<CellElement> cellElements

    RowElement(Row row) {
        this.node = row
        row.cells.each { cell ->
            cellElements << new CellElement(cell)
        }
    }

    boolean isFullyRendered() {
        cellElements.every { it.isFullyRendered }
    }
}
