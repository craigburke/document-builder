package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

class TableRenderer {


    Document document
    Table table

    TableRenderer(Table table, Document document) {
        this.document = document
        this.table = table
    }

    void render() {
        table.rows.each { renderRow(it) }
    }

    private void renderBorders(RowElement rowElement) {
        PDPageContentStream contentStream = document.item.contentStream
        setBorderOptions(contentStream)

        int xStart = document.margin.left + table.margin.left
        int xEnd = xStart + table.width

        int yTop = document.item.translateY(rowElement.startY)
        if (rowElement.node == table.rows.first() || rowElement.startY == document.margin.top) {
            contentStream.drawLine(xStart, yTop, xEnd, yTop)
        }

        int yBottom = document.item.translateY(document.item.y + table.padding)
        contentStream.drawLine(xStart, yBottom, xEnd, yBottom)

        int borderOffset = Math.floor(table.border.size.doubleValue() / 2)
        int offsetYTop = document.item.translateY(rowElement.startY - borderOffset)
        int offsetYBottom = document.item.translateY(document.item.y + table.padding + borderOffset)

        int currentX = xStart
        rowElement.cellElements.eachWithIndex { cellElement, i ->

            if (i == 0) {
                contentStream.drawLine(xStart, offsetYTop, xStart, offsetYBottom)
            }

            currentX += cellElement.node.width
            contentStream.drawLine(currentX, offsetYTop, currentX, offsetYBottom)
        }

    }

    private setBorderOptions(PDPageContentStream contentStream) {
        def borderColor = table.border.color.RGB
        contentStream.setStrokingColor(*borderColor)
        contentStream.setLineWidth(table.border.size)
    }

   private void renderRow(Row row) {
       Table table = row.parent
       int rowStartX = document.margin.left
       document.item.x = rowStartX

       RowElement rowElement = new RowElement(row)
       rowElement.startY = document.item.y

        while (!rowElement.fullyRendered) {
            document.item.x = rowStartX

            rowElement.cellElements.each { cellElement ->
                document.item.x += table.padding
                document.item.y = rowElement.startY + table.padding + table.border.size
                renderContentUntilEndPoint(cellElement)
                document.item.y += rowElement.renderedHeight
                document.item.x += cellElement.node.width + table.padding
            }

            if (rowElement.renderedHeight) {
                renderBorders(rowElement)
            }

            if (rowElement.fullyRendered) {
                document.item.y += table.padding
            }
            else {
                rowElement.startY = document.margin.top
                document.item.addPage()
                rowElement.renderedHeight = 0
            }
        }
   }

    private renderSideBorders(RowElement rowElement) {
        PDPageContentStream contentStream = document.item.contentStream
        setBorderOptions(contentStream)

        int xStart = document.margin.left
        int topOffset = Math.floor(table.border.size.doubleValue() / 2)

        int y = document.item.translateY(rowStartY - topOffset)

        int currentX = xStart
        rowElement.cellElements.eachWithIndex { cellElement, i ->

            int yBottom = document.item.translateY(rowStartY + rowElement.renderedHeight + (table.padding * 2) + (table.border.size * 2))

            if (i == 0) {
                contentStream.drawLine(xStart, y, xStart, yBottom)
            }

            currentX += cellElement.node.width
            contentStream.drawLine(currentX, y, currentX, yBottom)
        }

    }

    private void renderContentUntilEndPoint(CellElement cellElement) {
        boolean finished = false

        while (!finished) {
            ParagraphLine line = cellElement.currentLine

            if (canRenderCurrentLineOnPage(cellElement)) {
                int renderStartX = document.item.x
                ParagraphRenderer.renderLine(document, line, renderStartX)
                document.item.x = renderStartX
                cellElement.renderedHeight += line.height
            }
            else {
                cellElement.moveToPreviousLine()
                finished = true
            }

            if (!finished && cellElement.onLastLine) {
                cellElement.fullyRendered = true
                finished = true
            }
            else {
                cellElement.moveToNextLine()
            }

        }
    }

    boolean canRenderCurrentLineOnPage(CellElement cellElement) {
        ParagraphLine line = cellElement.currentLine
        Cell cell = cellElement.node
        Table table = cell.parent.parent

        int remainingHeight = document.item.remainingPageHeight

        int totalRequiredHeight = line.height + (cellElement.onLastLine ? table.padding : 0) + table.border.size

        (totalRequiredHeight <= remainingHeight)
    }

}
