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

    @Shared List<RowRenderer> rowRenderers

    def setup() {
        rowRenderers = []
        Document document = makeDocument()
        Table table = new Table(width: 800, padding: 0, border: [size: 0])
        table.parent = document
        document.children << table

        2.times { addRow(table) }

        PdfDocument pdfDocument = new PdfDocument(document)
        table.children.each {
            rowRenderers << new RowRenderer(it, pdfDocument, 0)
        }
    }

    private void addRow(Table table) {
        Row row = new Row(parent: table)
        table.children << row
        Cell rowspanCell = new Cell(parent: row, width: 200, rowspan: 2)
        Cell normalCell = new Cell(parent: row, width: 100)
        makeParagraph(5, rowspanCell)
        makeParagraph(5, normalCell)
        row.children << rowspanCell
        row.children << normalCell
    }

    def "rowspan height is set correctly after multiple parses"() {
        RowRenderer rowRenderer = rowRenderers[0]
        CellRenderer cellRenderer = rowRenderer.cellRenderers[0]

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
        RowRenderer rowRenderer = rowRenderers[0]
        CellRenderer cellRenderer = rowRenderer.cellRenderers[0]

        when:
        rowRenderer.parse(defaultLineHeight)
        rowRenderer.render(0)

        then:
        cellRenderer.rowspanHeight == defaultLineHeight

        when:
        rowRenderer.parse(defaultLineHeight)

        then:
        cellRenderer.currentRowHeight == defaultLineHeight

        and:
        cellRenderer.rowspanHeight == (defaultLineHeight * 2)

        when:
        rowRenderer.render(0)

        then:
        cellRenderer.rowspanHeight == (defaultLineHeight * 2)
    }

    def "parsedHeight is set correctly"() {
        RowRenderer rowRenderer = rowRenderers[0]
        CellRenderer cellRenderer = rowRenderer.cellRenderers[0]

        when:
        float partialHeight = defaultLineHeight * 3
        rowRenderer.parse(partialHeight)
        rowRenderer.render(0)

        then:
        cellRenderer.parsedHeight == 0

        and:
        cellRenderer.rowspanHeight == partialHeight

        when:
        rowRenderer.parse(defaultLineHeight)

        then:
        cellRenderer.parsedHeight == 0
        cellRenderer.rowspanHeight == partialHeight + defaultLineHeight
    }

}
