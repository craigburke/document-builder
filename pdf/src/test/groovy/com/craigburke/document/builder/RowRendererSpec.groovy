package com.craigburke.document.builder

import com.craigburke.document.builder.render.CellRenderer
import com.craigburke.document.builder.render.RowRenderer
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import spock.lang.Shared

/**
 * Cell renderer tests
 * @author Craig Burke
 */
class RowRendererSpec extends RendererTestBase {

    @Shared RowRenderer rowRenderer
    @Shared CellRenderer cellRenderer

    def setup() {
        Document document = makeDocument()
        Table table = new Table(width: 800, padding: 0, border: [size: 0])
        table.parent = document
        document.children << table

        Row row = new Row(parent: table)
        table.children << row
        Cell rowspanCell = new Cell(parent: row, width: 200, rowspan: 2)
        2.times {
            Cell normalCell = new Cell(parent:row, width: 100)
            makeParagraph(5, rowspanCell)
            makeParagraph(5, normalCell)
            row.children << rowspanCell
            row.children << normalCell
        }
        PdfDocument pdfDocument = new PdfDocument(document)
        rowRenderer = new RowRenderer(row, pdfDocument, 0)
        cellRenderer = rowRenderer.cellRenderers[0]
    }

    def "rowspan height is set correctly after multiple parses"() {
        when:
        float parseHeight = defaultLineHeight * 3f
        rowRenderer.parse(parseHeight)

        then:
        cellRenderer.rowspanHeight == parseHeight

        when:
        parseHeight = defaultLineHeight * 2f
        rowRenderer.parse(parseHeight)

        then:
        cellRenderer.rowspanHeight == parseHeight

        when:
        parseHeight = defaultLineHeight
        rowRenderer.parse(parseHeight)

        then:
        cellRenderer.rowspanHeight == parseHeight
    }

    def "rowspan height is updated after render"() {
        when:
        rowRenderer.parse(defaultLineHeight)
        rowRenderer.render(0)

        then:
        cellRenderer.rowspanHeight == defaultLineHeight

        when:
        rowRenderer.parse(defaultLineHeight)

        then:
        cellRenderer.rowspanHeight == (defaultLineHeight * 2)

        when:
        rowRenderer.render(0)

        then:
        cellRenderer.rowspanHeight == (defaultLineHeight * 2)
    }

    def "parsedHeight is set correctly"() {
        when:
        float rowHeight = defaultLineHeight * 5
        rowRenderer.parse(rowHeight)
        rowRenderer.render(0)

        then:
        cellRenderer.parsedHeight == 0

        when:
        rowRenderer.parse(rowHeight)
        rowRenderer.render(0)

        then:
        cellRenderer.parsedHeight == 0
        cellRenderer.rowspanHeight == (rowHeight * 2)
    }

}
