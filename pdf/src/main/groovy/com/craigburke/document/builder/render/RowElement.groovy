package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Column
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
    List<ColumnElement> columnElements = []

    RowElement(Row row, PdfDocument pdfDocument, float startX) {
        this.row = row
        this.startX = startX
        this.pdfDocument = pdfDocument

        Table table = row.parent
        float columnX = startX + table.border.size
        row.children.each { Column column ->
            columnElements << new ColumnElement(column, pdfDocument, columnX)
            columnX += column.width + table.border.size
        }
    }

    void parseUntilHeight(float height) {
        columnElements*.parseUntilHeight(height)
    }

    boolean getFullyParsed() {
        columnElements.every { it.fullyParsed }
    }

    float getTotalHeight() {
        columnElements*.totalHeight.max() ?: 0
    }

    float getParsedHeight() {
        columnElements*.parsedHeight.max() ?: 0
    }

    void renderElement(float startY) {
        renderBackgrounds(startY)
        renderBorders(startY)
        columnElements*.render(startY)
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
        columnElements.each { ColumnElement columnElement ->
            Column column = columnElement.column
            if (column.backgroundColor) {
                contentStream.setNonStrokingColor(*column.backgroundColor.rgb)
                float totalWidth = column.width + table.border.size
                float totalHeight = parsedHeight + table.border.size
                float startX = columnElement.startX - tableBorderOffset
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

        columnElements.eachWithIndex { columnElement, i ->
            if (i == 0) {
                float columnStartX = columnElement.startX - table.border.size
                contentStream.drawLine(columnStartX, translatedYTop, columnStartX, translatedYBottom)
            }
            float columnEndX = columnElement.startX + columnElement.column.width
            if (i == columnElements.size() - 1) {
                columnEndX += table.border.size
            }
            contentStream.drawLine(columnEndX, translatedYTop, columnEndX, translatedYBottom)
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
