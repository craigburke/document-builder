package com.craigburke.document.builder.render

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.builder.RenderState
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

/**
 * Rendering element for a Table node
 * @author Craig Burke
 */
class TableElement implements Renderable {

    Document document
    Table table
    List<RowElement> rowElements = []
    private int positionStart = 0
    private int positionEnd = 0

    TableElement(Table table, float startX) {
        this.table = table
        table.children.each { Row row ->
            rowElements << new RowElement(row, startX)
        }
    }

    void parseUntilHeight(float height) {
        if (!rowElements) {
            return
        }
        boolean reachedEnd = false
        float remainingHeight = height

        while (!reachedEnd) {
            RowElement rowElement = rowElements[positionEnd]
            rowElement.parseUntilHeight(remainingHeight)
            if (rowElement == rowElements.last() || !rowElement.fullyParsed) {
                reachedEnd = true
            }
            else {
                positionEnd++
            }
            remainingHeight -= rowElement.parsedHeight
        }
    }

    boolean getFullyParsed() {
        (rowElements) ? rowElements.every { it.fullyParsed } : true
    }

    float getTotalHeight() {
        rowElements.max { it.totalHeight }.totalHeight
    }

    float getParsedHeight() {
        rowElements.max { it.parsedHeight }.parsedHeight
    }

    float translateY(BigDecimal y) {
        document.element.translateY(y)
    }

    void render(Document document, RenderState renderState) {
        this.document = document
        rowElements[positionStart..positionEnd].each { renderRow(it, renderState) }
        positionStart = positionEnd
    }

    float getTableBorderOffset() {
        table.border.size.floatValue() / 2f
    }

    private void renderBackgrounds(RowElement rowElement) {
        float translatedStartY = translateY(rowElement.startY + rowElement.parsedHeight + tableBorderOffset)
        PDPageContentStream contentStream = document.element.contentStream
        rowElement.cellElements.each { CellElement cellElement ->
            Cell cell = cellElement.node
            if (cell.backgroundColor) {
                contentStream.setNonStrokingColor(*cell.backgroundColor.rgb)
                float totalWidth = cell.width + table.border.size
                float totalHeight = rowElement.parsedHeight + table.border.size
                float startX = cellElement.startX - tableBorderOffset
                contentStream.fillRect(startX, translatedStartY, totalWidth, totalHeight)
            }
        }
    }

    private void renderBorders(RowElement rowElement) {
        if (!table.border.size) {
            return
        }

        float translatedYTop = translateY(rowElement.startY - table.border.size)
        float translatedYBottom = translateY(rowElement.startY + rowElement.parsedHeight)
        float rowStartX = rowElement.startX - tableBorderOffset
        float rowEndX = rowElement.startX + table.width.floatValue() + tableBorderOffset

        PDPageContentStream contentStream = document.element.contentStream
        setBorderOptions(contentStream)

        if (shouldRenderTopBorder(rowElement)) {
            contentStream.drawLine(rowStartX, translatedYTop, rowEndX, translatedYTop)
        }

        if (rowElement.fullyParsed) {
            contentStream.drawLine(rowStartX, translatedYBottom, rowEndX, translatedYBottom)
        }

        rowElement.cellElements.eachWithIndex { cellElement, i ->
            if (i == 0) {
                float cellStartX = cellElement.startX - table.border.size
                contentStream.drawLine(cellStartX, translatedYTop, cellStartX, translatedYBottom)
            }
            float cellEndX = cellElement.startX + cellElement.node.width
            if (i == rowElement.cellElements.size() - 1) {
                cellEndX += table.border.size
            }
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
        rowElement.startY = document.element.y
        if (rowElement.firstRow) {
            rowElement.startY += table.border.size
        }

        document.element.x = rowElement.startX + table.border.size

        float height = document.element.remainingPageHeight
        rowElement.parseUntilHeight(height)
        renderBackgrounds(rowElement)
        rowElement.render(document, renderState)
        renderBorders(rowElement)

        if (!rowElement.fullyParsed) {
            rowElement.startY = document.margin.top
            if (rowElement.parsedHeight) {
                rowElement.spansMultiplePages = true
            }
        }

        document.element.y = rowElement.startY + rowElement.parsedHeight + table.border.size
    }

}
