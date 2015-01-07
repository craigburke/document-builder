package com.craigburke.document.builder

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Image
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.lowagie.text.pdf.PdfDocument
import groovy.xml.Namespace
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage

class PdfDocumentLoader {

    static Document load(byte[] data) {
        PDDocument pdfDoc = PDDocument.load(new ByteArrayInputStream(data))
        Document document = new Document(item: pdfDoc)

        def xmp = new XmlParser().parse(pdfDoc.documentCatalog.metadata.createInputStream())
        def rdf = new Namespace('http://www.w3.org/1999/02/22-rdf-syntax-ns#', 'rdf')
        def metaData = xmp[rdf.RDF][rdf.Description]['document'][0]

        document.marginTop = new BigDecimal(metaData.'@marginTop')
        document.marginBottom = new BigDecimal(metaData.'@marginBottom')
        document.marginLeft = new BigDecimal(metaData.'@marginLeft')
        document.marginRight = new BigDecimal(metaData.'@marginRight')

        metaData.each {
            if (it.name() == 'paragraph') {
                def paragraph = new Paragraph(parent: document)
                paragraph.marginTop = new BigDecimal(it.'@marginTop')
                paragraph.marginBottom = new BigDecimal(it.'@marginBottom')
                paragraph.marginLeft = new BigDecimal(it.'@marginLeft')
                paragraph.marginRight = new BigDecimal(it.'@marginRight')

                it.image.each {
                    paragraph.children << new Image(parent: paragraph)
                }

                document.children << paragraph

            }
            else {
                def table = new Table(parent: document, width: new BigDecimal(it.'@width'))
                it.row.each { rowNode ->
                    Row row = new Row()
                    rowNode.cell.each {
                        row.cells << new Cell(width: new BigDecimal(it.'@width'))
                    }
                    table.rows << row
                }
                document.children << table
            }
        }

        loadChildren(document)
        document
    }

    // The order of children could be wrong, might become an issue later
    private static void loadChildren(Document document) {
        def pages = document.item.documentCatalog.allPages

        // Images
        pages.resources.images*.each { name, image ->
            OutputStream out = new ByteArrayOutputStream()
            image.write2OutputStream(out)

            def imageNode
            document.children.each {
                imageNode = imageNode ?: it.children.find {it.getClass() == Image && !it.data}
            }

            imageNode?.data = out.toByteArray()
        }

        // Set content and margins based on text position
        def extractor = new PdfContentExtractor(document)
        pages.each { PDPage page ->
            if (page.contents) {
                extractor.processStream(page, page.findResources(), page.contents.stream)
            }
        }

    }




}