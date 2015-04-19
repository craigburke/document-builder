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
    List<ColumnElement> columnElements = []
    private boolean topBorderRendered = false

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

    void parse(float height) {
        columnElements*.parse(height)
    }

    boolean getFullyParsed() {
        columnElements.every { it.fullyParsed }
    }

    private float getPadding() {
        table.padding
    }

    float getTotalHeight() {
        float totalHeight = columnElements*.totalHeight.max()
        float totalBorder = row.parent.border.size * (firstRow ? 2 : 1)
        totalHeight + (padding * 2) + totalBorder
    }

    float getParsedHeight() {
        float parsedHeight = columnElements*.parsedHeight.max()
        if (firstRow) {
            parsedHeight += table.border.size
        }
        if (fullyParsed) {
            parsedHeight += table.border.size
        }

        parsedHeight
    }

    void renderElement(float startY) {
        renderBackgrounds(startY)
        renderBorders(startY)
        columnElements*.render(startY)
        renderCount++
    }

    private Table getTable() {
        row.parent
    }

    float getTableBorderOffset() {
        table.border.size.floatValue() / 2f
    }

    private void renderBackgrounds(float startY) {
        float backgroundStartY = startY + parsedHeight - tableBorderOffset
        if (!firstRow) {
            backgroundStartY += tableBorderOffset
        }
        if (!fullyParsed) {
            backgroundStartY -= table.border.size
        }

        float translatedStartY = pdfDocument.translateY(backgroundStartY)
        PDPageContentStream contentStream = pdfDocument.contentStream

        columnElements.each { ColumnElement columnElement ->
            Column column = columnElement.column
            if (column.backgroundColor) {
                boolean isLastColumn = (column == column.parent.children.last())
                contentStream.setNonStrokingColor(*column.backgroundColor.rgb)
                float startX = columnElement.startX - tableBorderOffset
                float width = column.width + (isLastColumn ? table.border.size : 0)
                float height = parsedHeight - (fullyParsed ? 0 : tableBorderOffset)
                height += ((fullyParsed && !onFirstPage) ? table.border.size : 0)
                contentStream.fillRect(startX, translatedStartY, width, height)
            }
        }
    }

    private void renderBorders(float startY) {
        if (!table.border.size) {
            return
        }

        float translatedYTop = pdfDocument.translateY(startY - tableBorderOffset)
        float translatedYBottom = pdfDocument.translateY(startY + parsedHeight)
        float rowStartX = startX - tableBorderOffset
        float rowEndX = startX + table.width.floatValue() + tableBorderOffset

        PDPageContentStream contentStream = pdfDocument.contentStream
        setBorderOptions(contentStream)

        if (!topBorderRendered) {
            contentStream.drawLine(rowStartX, translatedYTop, rowEndX, translatedYTop)
            topBorderRendered = true
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

    private setBorderOptions(PDPageContentStream contentStream) {
        def borderColor = table.border.color.rgb
        contentStream.setStrokingColor(*borderColor)
        contentStream.setLineWidth(table.border.size)
    }

    boolean isFirstRow() {
        (row == row.parent.children.first())
    }

}
