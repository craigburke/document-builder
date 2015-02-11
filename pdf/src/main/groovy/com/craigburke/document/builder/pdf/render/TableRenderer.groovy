package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

class TableRenderer {

    int renderStartY

    Document document
    Table table

    TableRenderer(Table table, Document document) {
        this.document = document
        this.table = table
    }

    void render() {
        renderStartY = document.item.translatedY
        table.rows.each { renderRow(it) }
    }

   private void renderRow(Row row) {
       int rowStartX = document.margin.left
       document.item.x = rowStartX

       RowElement rowElement = new RowElement(row)

        while (!rowElement.fullyRendered) {
            document.item.x = rowStartX

            rowElement.cellElements.each { cellElement ->
                document.item.x += cellElement.node.padding
                document.item.y = renderStartY + cellElement.node.padding
                renderContentUntilEndPoint(cellElement)
                document.item.x += cellElement.node.width + cellElement.node.padding
            }

            int maxCellPadding = row.cells.max { it.padding }.padding
            renderStartY += rowElement.renderedHeight + maxCellPadding

            if (!rowElement.fullyRendered) {
                renderStartY = document.margin.top
                document.item.addPage()
                rowElement.renderedHeight = 0
            }
        }
    }

    private void renderContentUntilEndPoint(CellElement cellElement) {
        boolean finished = false

        while (!finished) {
            cellElement.moveToNextLine()
            ParagraphLine line = cellElement.currentLine

            if (line.height > document.item.remainingPageHeight) {
                cellElement.moveToPreviousLine()
                finished = true
            }
            else {
                int renderStartX = document.item.x
                ParagraphRenderer.renderLine(document, line, renderStartX)
                document.item.x = renderStartX
                cellElement.renderedHeight += line.height
            }

            if (cellElement.onLastLine) {
                cellElement.fullyRendered = true
                finished = true
            }
        }
    }

}
