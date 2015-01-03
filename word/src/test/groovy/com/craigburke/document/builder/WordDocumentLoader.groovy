package com.craigburke.document.builder

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.Image
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.Text
import org.apache.poi.xwpf.usermodel.XWPFDocument

import static com.craigburke.document.core.UnitUtil.twipToPoint

class WordDocumentLoader {

    static Document load(byte[] data) {
        Document document = new Document()
        document.item = new XWPFDocument(new ByteArrayInputStream(data))

        def documentMargin = document.item.document.body.sectPr.pgMar
        document.marginTop = twipToPoint(documentMargin.top)
        document.marginBottom = twipToPoint(documentMargin.bottom)
        document.marginLeft = twipToPoint(documentMargin.left)
        document.marginRight = twipToPoint(documentMargin.right)

        loadParagraphs(document)
        loadTables(document)
        document
    }

    static private loadParagraphs(Document document) {
        document.children += document.item.paragraphs.collect { getParagraph(it) }
    }

    static private loadTables(Document document) {

        document.item.tables.each { tableItem ->
            Table table = new Table(item: tableItem, width: twipToPoint(tableItem.width), parent: document)

            tableItem.rows.each { rowItem ->
                Row row = new Row(item: rowItem, parent: table)
                table.rows << row
                rowItem.tableCells.each { cellItem ->
                    Cell cell = new Cell(item: cellItem, parent: row)

                    def widthSettings = cellItem.CTTc.tcPr?.tcW
                    if (widthSettings) {
                        cell.width = twipToPoint(widthSettings.w)
                    }

                    cell.paragraphs = cell.item.paragraphs.collect { getParagraph(it) }

                    row.cells << cell
                }
            }

            document.children += table
        }
    }

    static private Paragraph getParagraph(paragraph) {
        Paragraph p = new Paragraph(item: paragraph)
        p.children = getParagraphChildren(p)
        p.marginBottom = twipToPoint(paragraph.spacingAfter)
        p.marginTop = twipToPoint(paragraph.spacingBefore)

        def indent = paragraph.CTP.PPr.ind
        p.marginLeft = twipToPoint(indent.left)
        p.marginRight = twipToPoint(indent.right)

        p
    }


    static private List getParagraphChildren(Paragraph paragraph) {
        def items = []

        paragraph.item.runs.each { run ->
            if (run.embeddedPictures) {
                items << new Image(data: run.embeddedPictures[0].pictureData.data, parent: paragraph)
            }
            else {
                def text = new Text(value: run.toString(), parent: paragraph)
                text.font = new Font(family: run.fontFamily, size: run.fontSize)
                items << text
            }
        }

        items
    }

}
