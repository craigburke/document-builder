package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream

/**
 * Rendering element for the Row node
 * @author Craig Burke
 */
class RowElement implements Renderable {

    Row row
    boolean spansMultiplePages = false
    List<CellElement> cellElements = []

    RowElement(Row row, PdfDocument pdfDocument, float startX) {
        this.row = row
        this.startX = startX
        this.pdfDocument = pdfDocument

        Table table = row.parent
        float cellX = startX + table.border.size
        row.children.each { Cell cell ->
            cellElements << new CellElement(cell, pdfDocument, cellX)
            cellX += cell.width + table.border.size
        }
    }

    void parseUntilHeight(float height) {
        cellElements*.parseUntilHeight(height)
    }

    boolean getFullyParsed() {
        cellElements.every { it.fullyParsed }
    }

    float getTotalHeight() {
        cellElements*.totalHeight.max() ?: 0
    }

    float getParsedHeight() {
        cellElements*.parsedHeight.max() ?: 0
    }

    void renderElement(float startY) {
        renderBackgrounds(startY)
        renderBorders(startY)
        cellElements*.render(startY)
        if (!fullyParsed) {
            if (parsedHeight) {
                spansMultiplePages = true
            }
        }
    }

    private Table getTable() {
        row.parent
    }

    float getTableBorderOffset() {
        table.border.size.floatValue() / 2f
    }

    private void renderBackgrounds(float startY) {
        float translatedStartY = pdfDocument.translateY(startY + parsedHeight + tableBorderOffset)
        PDPageContentStream contentStream = pdfDocument.contentStream
        cellElements.each { CellElement cellElement ->
            Cell cell = cellElement.cell
            if (cell.backgroundColor) {
                contentStream.setNonStrokingColor(*cell.backgroundColor.rgb)
                float totalWidth = cell.width + table.border.size
                float totalHeight = parsedHeight + table.border.size
                float startX = cellElement.startX - tableBorderOffset
                contentStream.fillRect(startX, translatedStartY, totalWidth, totalHeight)
            }
        }
    }

    private void renderBorders(float startY) {
        if (!table.border.size) {
            return
        }

        float translatedYTop = pdfDocument.translateY(startY - table.border.size)
        float translatedYBottom = pdfDocument.translateY(startY + parsedHeight)
        float rowStartX = startX - tableBorderOffset
        float rowEndX = startX + table.width.floatValue() + tableBorderOffset

        PDPageContentStream contentStream = pdfDocument.contentStream
        setBorderOptions(contentStream)

        if (shouldRenderTopBorder()) {
            contentStream.drawLine(rowStartX, translatedYTop, rowEndX, translatedYTop)
        }

        if (fullyParsed) {
            contentStream.drawLine(rowStartX, translatedYBottom, rowEndX, translatedYBottom)
        }

        cellElements.eachWithIndex { cellElement, i ->
            if (i == 0) {
                float cellStartX = cellElement.startX - table.border.size
                contentStream.drawLine(cellStartX, translatedYTop, cellStartX, translatedYBottom)
            }
            float cellEndX = cellElement.startX + cellElement.cell.width
            if (i == cellElements.size() - 1) {
                cellEndX += table.border.size
            }
            contentStream.drawLine(cellEndX, translatedYTop, cellEndX, translatedYBottom)
        }
    }

    private boolean shouldRenderTopBorder() {
        if (row == table.children.first()) {
            true
        }
        else if (pdfDocument.y == pdfDocument.document.margin.top && !spansMultiplePages) {
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

}
