package com.craigburke.document.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.Image
import com.craigburke.document.core.Paragraph
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

        loadChildren(document)
        document
    }

    // The order of children could be wrong, might become an issue later
    private static void loadChildren(Document document) {
        def pages = document.item.documentCatalog.allPages

        // Images
        pages.resources.images*.each { name, image ->
            Paragraph paragraph = new Paragraph(parent: document)
            OutputStream out = new ByteArrayOutputStream()
            image.write2OutputStream(out)
            paragraph.children << new Image(data: out.toByteArray())
            document.children << paragraph
        }

        // Paragraphs
        def extractor = new PdfParagraphExtractor(document)
        pages.each { PDPage page ->
            if (page.contents) {
                extractor.processStream(page, page.findResources(), page.contents.stream)
            }
            document.children.addAll(0, extractor.paragraphs)
        }

        // Tables

    }




}