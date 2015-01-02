package com.craigburke.document.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.Image
import com.craigburke.document.core.Paragraph
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage

class PdfDocumentLoader {

    static Document load(byte[] data) {
        PDDocument pdfDoc = PDDocument.load(new ByteArrayInputStream(data))
        Document document = new Document(item: pdfDoc)

        def info = pdfDoc.documentInformation.info
        document.marginTop = new BigDecimal(info.getString('marginTop'))
        document.marginLeft = new BigDecimal(info.getString('marginLeft'))
        document.marginBottom = new BigDecimal(info.getString('marginBottom'))
        document.marginRight = new BigDecimal(info.getString('marginRight'))

        loadParagraphs(document)
        loadTables(document)
        document
    }

    // The order of paragraphs could be wrong, might become an issue later
    private static void loadParagraphs(Document document) {

        def pages = document.item.documentCatalog.allPages

        pages.resources.images*.each { name, image ->
            Paragraph paragraph = new Paragraph(parent: document)
            OutputStream out = new ByteArrayOutputStream()
            image.write2OutputStream(out)
            paragraph.children << new Image(data: out.toByteArray())
            document.children << paragraph
        }

        def extractor = new PdfParagraphExtractor(document)
        pages.each { PDPage page ->
            if (page.contents) {
                extractor.processStream(page, page.findResources(), page.contents.stream)
            }
            document.children.addAll(0, extractor.paragraphs)
        }
    }

    private static void loadTables(Document document) {

    }



}