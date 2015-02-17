package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Border
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

class TableRenderer {

    int rowStartY

    Document document
    Table table

    TableRenderer(Table table, Document document) {
        this.document = document
        this.table = table
    }

    void render() {
        rowStartY = document.item.translatedY
        renderTopTableBorder()
        table.rows.each { renderRow(it) }
    }

    private void renderTopTableBorder() {
        PDPageContentStream contentStream = document.item.contentStream
        int xStart = document.margin.left
        int xEnd = table.width + xStart
        int translatedY = document.item.translateY(rowStartY)

        contentStream.drawLine(xStart, translatedY, xEnd, translatedY)
    }

   private void renderRow(Row row) {
       Table table = row.parent
       int rowStartX = document.margin.left
       document.item.x = rowStartX

       RowElement rowElement = new RowElement(row)

        while (!rowElement.fullyRendered) {
            document.item.x = rowStartX

            rowElement.cellElements.each { cellElement ->
                Cell cell = cellElement.node

                document.item.x += cellElement.node.padding
                document.item.y = rowStartY + cell.padding + table.border.size
                renderContentUntilEndPoint(cellElement)
                document.item.x += cellElement.node.width + cellElement.node.padding
            }
            renderBorders(rowElement, table.border)

            rowStartY += rowElement.renderedHeight + rowElement.node.maxCellPadding

            if (!rowElement.fullyRendered) {
                rowStartY = document.margin.top
                document.item.addPage()
                rowElement.renderedHeight = 0
            }
        }
    }

    private renderBorders(RowElement rowElement, Border border) {
        Table table = rowElement.node.parent
        PDPageContentStream contentStream = document.item.contentStream

        int xStart = document.margin.left
        int xEnd = xStart + table.width
        int y = document.item.translateY(rowStartY)

        int yBottom = document.item.translateY(rowStartY + rowElement.renderedHeight + (rowElement.node.maxCellPadding * 2))
        contentStream.drawLine(xStart, y, xStart, yBottom)
        contentStream.drawLine(xEnd, y, xEnd, yBottom)

        contentStream.drawLine(xStart, yBottom, xEnd, yBottom)

        int currentX = xStart
        rowElement.cellElements.each {
            currentX += it.node.width + (it.node.padding * 2)
            contentStream.drawLine(currentX, y, currentX, yBottom)
        }

    }

    private void renderContentUntilEndPoint(CellElement cellElement) {
        boolean finished = false

        while (!finished) {
            ParagraphLine line = cellElement.currentLine
            int remainingHeight = document.item.remainingPageHeight

            if (remainingHeight < line?.height) {
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
            else {
                cellElement.moveToNextLine()
            }

        }
    }

}
