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
    private List<RowElement> rowElements = []

    TableRenderer(Table table, Document document) {
        this.document = document
        this.table = table
        int startX = document.item.x
        table.children.each { Row row ->
            rowElements << new RowElement(row, startX)
        }
    }

    int getTotalHeight() {
        int height = table.margin.top + table.margin.bottom

        rowElements.each {
            height += it.totalHeight
        }

        height
    }

    void render(RenderState renderState = RenderState.PAGE) {
        rowElements.each { renderRow(it, renderState) }
    }

    float translateY(BigDecimal y) {
        document.item.translateY(y)
    }

    private void renderBorders(RowElement rowElement) {
        float borderOffset = table.border.size.floatValue() / 2f

        float translatedYTop = translateY(rowElement.startY - table.border.size)
        float translatedYBottom = translateY(rowElement.startY + rowElement.renderedHeight.floatValue())
        float rowStartX = rowElement.startX - borderOffset
        float rowEndX = rowElement.startX + table.width.floatValue() + borderOffset

        PDPageContentStream contentStream = document.item.contentStream
        setBorderOptions(contentStream)

        if (shouldRenderTopBorder(rowElement)) {
            contentStream.drawLine(rowStartX, translatedYTop, rowEndX, translatedYTop)
        }

        if (rowElement.fullyRendered) {
            contentStream.drawLine(rowStartX, translatedYBottom, rowEndX, translatedYBottom)
        }

        rowElement.cellElements.eachWithIndex { cellElement, i ->
            if (i == 0) {
                float cellStartX = cellElement.startX - table.border.size as float
                contentStream.drawLine(cellStartX, translatedYTop, cellStartX, translatedYBottom)
            }
            float cellEndX = cellElement.startX + cellElement.node.width + table.border.size as float
            contentStream.drawLine(cellEndX, translatedYTop, cellEndX, translatedYBottom)
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

   private void renderRow(RowElement rowElement, RenderState renderState) {
       rowElement.startX = document.margin.left + table.margin.left
       rowElement.startY = document.item.y
       if (rowElement.firstRow) {
           rowElement.startY += table.border.size
       }

        while (!rowElement.fullyRendered) {
            document.item.x = rowElement.startX + table.border.size

            rowElement.cellElements.each {
                document.item.y = rowElement.startY + table.padding
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
                    cellElement.renderedHeight += table.padding
                }

                int renderStartX = cellStartX + table.padding
                ParagraphRenderer.renderLine(document, line, renderStartX, renderState)
                cellElement.renderedHeight += line.contentHeight + line.lineSpacing

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

        int totalRequiredHeight = line.totalHeight

        if (cellElement.onFirstLine) {
            totalRequiredHeight += table.padding + table.border.size
        }

        (totalRequiredHeight <= remainingHeight)
    }

}
