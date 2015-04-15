package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

/**
 * Rendering element for a Table node
 * @author Craig Burke
 */
class TableElement implements Renderable {

    private final float startX
    private final float startY

    Table table
    List<RowElement> rowElements = []
    private int rowStart = 0
    private int rowEnd = 0

    TableElement(Table table, PdfDocument pdfDocument, float startX, float startY) {
        this.startX = startX
        this.startY = startY
        this.pdfDocument = pdfDocument

        this.table = table
        float rowStartY = startY
        table.children.each { Row row ->
            RowElement rowElement = new RowElement(row, pdfDocument, startX, rowStartY)
            rowStartY += rowElement.totalHeight
            rowStartY = pdfDocument.translateAbsoluteY(rowStartY)
            rowElements << rowElement
        }
    }

    void parseUntilHeight(float height) {
        if (!rowElements) {
            return
        }
        rowStart = rowEnd
        boolean reachedEnd = false
        float remainingHeight = height

        while (!reachedEnd) {
            RowElement rowElement = rowElements[rowEnd]
            rowElement.parseUntilHeight(remainingHeight)

            if (rowElement == rowElements.last()) {
                reachedEnd = true
            }
            if (!rowElement.fullyParsed) {
                reachedEnd = true
            }
            else if (rowEnd != rowElements.size() - 1) {
                rowEnd++
            }

            remainingHeight -= rowElement.parsedHeight
        }
    }

    boolean getFullyParsed() {
        (rowElements) ? rowElements.every { it.fullyParsed } : true
    }

    float getTotalHeight() {
        rowElements.max { it.totalHeight }.totalHeight ?: 0
    }

    float getParsedHeight() {
        rowElements.max { it.parsedHeight }.parsedHeight ?: 0
    }

    void render() {
        rowElements[rowStart..rowEnd].each { renderRow(it) }
    }

    float getTableBorderOffset() {
        table.border.size.floatValue() / 2f
    }

    private void renderBackgrounds(RowElement rowElement) {
        float translatedStartY = pdfDocument.translateY(rowElement.startY + rowElement.parsedHeight + tableBorderOffset)
        PDPageContentStream contentStream = pdfDocument.contentStream
        rowElement.cellElements.each { CellElement cellElement ->
            Cell cell = cellElement.cell
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

        float translatedYTop = pdfDocument.translateY(rowElement.startY - table.border.size)
        float translatedYBottom = pdfDocument.translateY(rowElement.startY + rowElement.parsedHeight)
        float rowStartX = rowElement.startX - tableBorderOffset
        float rowEndX = rowElement.startX + table.width.floatValue() + tableBorderOffset

        PDPageContentStream contentStream = pdfDocument.contentStream
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
            float cellEndX = cellElement.startX + cellElement.cell.width
            if (i == rowElement.cellElements.size() - 1) {
                cellEndX += table.border.size
            }
            contentStream.drawLine(cellEndX, translatedYTop, cellEndX, translatedYBottom)
        }
    }

    private boolean shouldRenderTopBorder(RowElement rowElement) {
        if (rowElement.row == table.children.first()) {
            true
        }
        else if (rowElement.startY == pdfDocument.document.margin.top && !rowElement.spansMultiplePages) {
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

    private void renderRow(RowElement rowElement) {
        pdfDocument.y = rowElement.startY
        if (rowElement.firstRow) {
            pdfDocument.y += table.border.size
        }
        renderBackgrounds(rowElement)
        rowElement.render()
        renderBorders(rowElement)

        if (!rowElement.fullyParsed) {
            if (rowElement.parsedHeight) {
                rowElement.spansMultiplePages = true
            }
        }

    }

}
