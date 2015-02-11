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
       document.item.x = document.margin.left

       RowElement rowElement = new RowElement(row)

        while (!rowElement.fullyRendered) {
            document.item.x = document.margin.left

            rowElement.cellElements.each { cellElement ->
                document.item.y = renderStartY
                renderContentUntilEndPoint(cellElement)
                document.item.x += cellElement.node.width
            }

            renderStartY += rowElement.renderedHeight

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

            if (!finished) {
                cellElement.moveToNextLine()
            }

        }
    }

}
