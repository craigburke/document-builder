package com.craigburke.document.builder.render

import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.builder.RenderState
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

    void render(RenderState renderState) {
        table.children.each { renderRow(it, renderState) }
    }

    int translateY(int y) {
        document.item.translateY(y)
    }

    private void renderBorders(RowElement rowElement) {
        document.item.y = rowElement.startY

        int borderOffset = Math.floor(table.border.size.doubleValue() / 2)

        PDPageContentStream contentStream = document.item.contentStream
        setBorderOptions(contentStream)

        int xStart = document.margin.left + table.margin.left
        int xEnd = xStart + table.width

        int yTop = rowElement.startY

        if (shouldRenderTopBorder(rowElement)) {
            contentStream.drawLine(xStart, translateY(yTop), xEnd, translateY(yTop))
        }

        int yBottom
        if (rowElement.fullyRendered) {
            yBottom = document.item.y + rowElement.renderedHeight + table.border.size
            contentStream.drawLine(xStart, translateY(yBottom), xEnd, translateY(yBottom))
        }
        else {
           yBottom = document.item.y + document.item.remainingPageHeight
        }

        int offsetYBottom = yBottom + borderOffset

        int currentX = document.margin.left + table.margin.left + borderOffset
        rowElement.cellElements.eachWithIndex { cellElement, i ->
            if (i == 0) {
                contentStream.drawLine(currentX, translateY(yTop), currentX, translateY(offsetYBottom))
            }
            currentX += cellElement.node.width

            if (i == rowElement.cellElements.size() - 1) {
                currentX = xEnd - borderOffset
            }
            else {
                currentX += table.border.size
            }

            contentStream.drawLine(currentX, translateY(yTop), currentX, translateY(offsetYBottom))
        }

    }

    private boolean shouldRenderTopBorder(RowElement rowElement) {
        if (rowElement.node == table.children.first()) {
            true
        }
        else if (rowElement.startY == document.margin.top && !rowElement.spansMultiplePages) {
            true
        }
        else {
            false
        }

    }

    private setBorderOptions(PDPageContentStream contentStream) {
        def borderColor = table.border.color.rgb
        contentStream.setStrokingColor(*borderColor)
        contentStream.setLineWidth(table.border.size)
    }

   private void renderRow(Row row, RenderState renderState) {
       int rowStartX = document.margin.left + table.margin.left

       RowElement rowElement = new RowElement(row)

       rowElement.startY = document.item.y
       if (rowElement.firstRow) {
           rowElement.startY += table.border.size
       }

        while (!rowElement.fullyRendered) {
            document.item.x = rowStartX + table.border.size

            rowElement.cellElements.each {
                document.item.y = rowElement.startY
                renderContentUntilEndPoint(it, renderState)
            }

            if (rowElement.renderedHeight && table.border.size) {
                renderBorders(rowElement)
            }

            if (!rowElement.fullyRendered) {
                rowElement.startY = document.margin.top
                if (rowElement.renderedHeight) {
                    rowElement.spansMultiplePages = true
                }
                document.item.addPage()
                rowElement.renderedHeight = 0
            }
        }

        document.item.y = rowElement.startY + rowElement.renderedHeight + table.border.size
   }

    private void renderContentUntilEndPoint(CellElement cellElement, RenderState renderState) {
        boolean reachedBottomOfPage = false
        int cellStartX = document.item.x

        while (!reachedBottomOfPage && !cellElement.fullyRendered) {
            ParagraphLine line = cellElement.currentLine

            if (canRenderCurrentLineOnPage(cellElement, renderState)) {
                if (cellElement.onFirstLine) {
                    document.item.y += table.padding
                    cellElement.renderedHeight += table.padding
                }

                int renderStartX = cellStartX + table.padding
                ParagraphRenderer.renderLine(document, line, renderStartX, renderState)
                cellElement.renderedHeight += line.height

                if (cellElement.onLastLine) {
                    cellElement.renderedHeight += table.padding
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

    boolean canRenderCurrentLineOnPage(CellElement cellElement, RenderState renderState) {
        if (renderState != RenderState.PAGE) {
            return true
        }

        ParagraphLine line = cellElement.currentLine

        int remainingHeight = document.item.remainingPageHeight

        int totalRequiredHeight = line.height

        if (cellElement.onFirstLine) {
            totalRequiredHeight += table.padding + table.border.size
        }

        (totalRequiredHeight <= remainingHeight)
    }

}
