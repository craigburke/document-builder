package com.craigburke.document.builder.render

import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

/**
 * Renders a table to the document
 * @author Craig Burke
 */
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
        document.item.y = rowElement.startY

        int borderOffset = Math.floor(table.border.size.doubleValue() / 2)

        PDPageContentStream contentStream = document.item.contentStream
        setBorderOptions(contentStream)

        int xStart = document.margin.left + table.margin.left
        int xEnd = xStart + table.width

        int yTop = document.item.translateY(rowElement.startY + borderOffset)

        if (!rowElement.spansMultiplePages) {
            contentStream.drawLine(xStart, yTop, xEnd, yTop)
        }

        int yBottom = document.item.translateY(document.item.y + rowElement.renderedHeight)

        if (rowElement.node == table.rows.last() && rowElement.fullyRendered) {
            contentStream.drawLine(xStart, yBottom, xEnd, yBottom)
        }

        int offsetYBottom = document.item.translateY(document.item.y + rowElement.renderedHeight + borderOffset)

        int currentX = document.margin.left + table.margin.left + borderOffset
        rowElement.cellElements.eachWithIndex { cellElement, i ->
            if (i == 0) {
                contentStream.drawLine(currentX, yTop, currentX, offsetYBottom)
            }
            currentX += cellElement.node.width

            if (i == rowElement.cellElements.size() - 1) {
                currentX = xEnd - borderOffset
            }
            else {
                currentX += table.border.size
            }

            contentStream.drawLine(currentX, yTop, currentX, offsetYBottom)
        }

    }

    private setBorderOptions(PDPageContentStream contentStream) {
        def borderColor = table.border.color.rgb
        contentStream.setStrokingColor(*borderColor)
        contentStream.setLineWidth(table.border.size)
    }

   private void renderRow(Row row) {
       int rowStartX = document.margin.left

       RowElement rowElement = new RowElement(row)
       rowElement.startY = document.item.y

        while (!rowElement.fullyRendered) {
            document.item.x = rowStartX + table.border.size

            rowElement.cellElements.each {
                document.item.y = rowElement.startY
                renderContentUntilEndPoint(it)
            }

            if (rowElement.renderedHeight) {
                renderBorders(rowElement)
            }

            if (!rowElement.fullyRendered) {
                rowElement.startY = document.margin.top
                rowElement.spansMultiplePages = true
                document.item.addPage()
                rowElement.renderedHeight = 0
            }
        }

        document.item.y = rowElement.startY + rowElement.renderedHeight
   }

    private void renderContentUntilEndPoint(CellElement cellElement) {
        boolean reachedBottomOfPage = false
        int cellStartX = document.item.x

        while (!reachedBottomOfPage && !cellElement.fullyRendered) {
            ParagraphLine line = cellElement.currentLine

            if (canRenderCurrentLineOnPage(cellElement)) {
                if (cellElement.onFirstLine) {
                    document.item.y += table.border.size + table.padding
                    cellElement.renderedHeight += table.padding + table.border.size
                }

                int renderStartX = cellStartX + table.padding + table.border.size
                ParagraphRenderer.renderLine(document, line, renderStartX)
                cellElement.renderedHeight += line.height

                if (cellElement.onLastLine) {
                    cellElement.renderedHeight += table.padding + table.border.size
                    cellElement.fullyRendered = true
                }
            }
            else {
                cellElement.moveToPreviousLine()
                reachedBottomOfPage = true
            }

            if (!reachedBottomOfPage && !cellElement.fullyRendered) {
                cellElement.moveToNextLine()
            }

        }

        document.item.x = cellStartX + cellElement.node.width + table.border.size
    }

    boolean canRenderCurrentLineOnPage(CellElement cellElement) {
        ParagraphLine line = cellElement.currentLine

        int remainingHeight = document.item.remainingPageHeight

        int totalRequiredHeight = line.height

        if (cellElement.onFirstLine) {
            totalRequiredHeight += table.padding + table.border.size
        }

        (totalRequiredHeight <= remainingHeight)
    }

}
