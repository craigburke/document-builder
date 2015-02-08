package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table

class TableRenderer {

    int renderStartY

    Document document
    Table table

    TableRenderer(Table table, Document document) {
        renderStartY = document.item.translatedY
        this.document = document
        this.table = table
    }

    void render() {
        table.rows.each { renderRow(it) }
    }

    private void renderRow(Row row) {
        RowElement rowElement = new RowElement(row)

        while (!rowElement.fullyRendered) {
            rowElement.cellElements.each { cellElement ->
                renderUntilEndPoint(cellElement)
                document.x = cellElement
            }
            if (!rowElement.fullyRendered) {
                renderStartY = document.margin.top
                document.item.addPage()
            }
        }

    }

    private void renderUntilEndPoint(CellElement cellElement) {
        boolean finished = false

        while (!finished) {
            ParagraphLine line = cellElement.nextLine
            ParagraphRenderer.renderLine(document, line)

            if (cellElement.fullyRendered || line.height > document.item.remainingPageHeight) {
                finished = true
            }
        }
    }

}
