package com.craigburke.document.builder

import com.craigburke.document.builder.render.TableRenderer
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.Margin
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.TextBlock
import spock.lang.Shared

/**
 * Tables element tests
 * @author Craig Burke
 */
class TableRendererSpec extends RendererTestBase {

    @Shared Table table
    @Shared TableRenderer tableRenderer
    @Shared float defaultRowHeight
    @Shared int rowCount = 2

    def setup() {
        table = new Table(margin:Margin.NONE, padding:20, border:[size:3], columns:[1])
        TextBlock paragraph = makeParagraph(5)
        paragraph.margin = Margin.NONE
        tableRenderer = makeTableElement(table, paragraph, rowCount)
        defaultRowHeight = (defaultLineHeight * 5f) + (table.padding * 2f) + (table.border.size)
    }

    def "parse first row"() {
        float firstRowHeight = defaultRowHeight + table.border.size

        when:
        tableRenderer.parse(firstRowHeight)

        then:
        tableRenderer.parsedHeight == firstRowHeight

        and:
        tableRenderer.parseStart == 0

        and:
        tableRenderer.parseEnd == 0
    }

    def "parse part of first row"() {
        float partialRowHeight = table.padding + (defaultLineHeight * 3) + table.border.size

        when:
        tableRenderer.parse(partialRowHeight)

        then:
        tableRenderer.parseStart == 0

        and:
        tableRenderer.parseEnd == 0

        and:
        tableRenderer.parsedHeight == partialRowHeight
    }

    def "parse all rows"() {
        float totalHeight = (rowCount * defaultRowHeight) + table.border.size

        when:
        tableRenderer.parse(totalHeight)

        then:
        tableRenderer.parsedHeight == totalHeight

        and:
        tableRenderer.fullyParsed
    }

    private TableRenderer makeTableElement(Table table, TextBlock paragraph, int rows) {
        Document tableDocument = makeDocument()
        table.parent = tableDocument
        int cellCount = table.columns.size()

        rows.times {
            Row row = new Row(font:new Font())
            row.parent = table
            table.children << row
            cellCount.times {
                Cell cell = new Cell(font:new Font())
                row.children << cell
                cell.parent = row
                makeParagraph(paragraph, cell)
            }
        }
        table.updateRowspanColumns()
        table.normalizeColumnWidths()

        PdfDocument pdfDocument = new PdfDocument(tableDocument)
        new TableRenderer(table, pdfDocument, 0)
    }
}
