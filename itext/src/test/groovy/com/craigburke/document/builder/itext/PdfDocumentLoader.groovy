package com.craigburke.document.builder.itext

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Image
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage

class PdfDocumentLoader {

    static Document load(byte[] data) {
        PDDocument pdfDoc = PDDocument.load(new ByteArrayInputStream(data))
        Document document = new Document(item: pdfDoc)

        def metaData = new XmlParser().parse(pdfDoc.documentCatalog.metadata.createInputStream())

        document.margin.top = new BigDecimal(metaData.'@marginTop')
        document.margin.bottom = new BigDecimal(metaData.'@marginBottom')
        document.margin.left = new BigDecimal(metaData.'@marginLeft')
        document.margin.right = new BigDecimal(metaData.'@marginRight')

        metaData.each {
            if (it.name() == 'paragraph') {
                def paragraph = new Paragraph(parent: document)
                paragraph.margin.top = new BigDecimal(it.'@marginTop')
                paragraph.margin.bottom = new BigDecimal(it.'@marginBottom')
                paragraph.margin.left = new BigDecimal(it.'@marginLeft')
                paragraph.margin.right = new BigDecimal(it.'@marginRight')

                it.image.each {
                    paragraph.children << new Image(parent: paragraph)
                }

                document.children << paragraph

            }
            else {
                def table = new Table(parent: document, width: new BigDecimal(it.'@width'))
                it.row.each { rowNode ->
                    Row row = new Row()
                    rowNode.cell.each { cellNode ->
                        def cell = new Cell(width: new BigDecimal(cellNode.'@width'))
                        cell.children << new Paragraph()
                        row.cells << cell
                    }
                    table.rows << row
                }
                document.children << table
            }
        }

        loadChildren(document)
        pdfDoc.close()
        document
    }

    private static void loadChildren(Document document) {
        def pages = document.item.documentCatalog.allPages

        // Set content and margins based on text position
        def extractor = new PdfContentExtractor(document)
        pages.each { PDPage page ->
            if (page.contents) {
                extractor.processStream(page, page.findResources(), page.contents.stream)
            }
        }

    }

}