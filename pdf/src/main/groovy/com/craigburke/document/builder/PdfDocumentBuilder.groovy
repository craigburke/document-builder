package com.craigburke.document.builder

import com.craigburke.document.builder.render.ParagraphRenderer
import com.craigburke.document.builder.render.TableRenderer
import com.craigburke.document.core.EmbeddedFont
import com.craigburke.document.core.HeaderFooterOptions
import com.craigburke.document.core.PageBreak
import com.craigburke.document.core.builder.RenderState
import groovy.transform.InheritConstructors
import groovy.xml.MarkupBuilder

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.Document
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Table
import com.craigburke.document.core.Image

import org.apache.pdfbox.pdmodel.common.PDMetadata

/**
 * Builder for PDF documents
 * @author Craig Burke
 */
@InheritConstructors
class PdfDocumentBuilder extends DocumentBuilder {

    PdfDocument pdfDocument

    void initializeDocument(Document document, OutputStream out) {
        pdfDocument = new PdfDocument(document)
        pdfDocument.x = document.margin.left
        pdfDocument.y = document.margin.top
        document.element = pdfDocument
    }

    def addEmbeddedFont = { EmbeddedFont embeddedFont ->
        PdfFont.addFont(pdfDocument.pdDocument, embeddedFont)
    }

    def addPageBreakToDocument = { PageBreak pageBreak, Document document ->
        pdfDocument.addPage()
    }

    def onTextBlockComplete = { TextBlock paragraph ->
        if (renderState == RenderState.PAGE && paragraph.parent instanceof Document) {
            int pageWidth = document.width - document.margin.left - document.margin.right
            int maxLineWidth = pageWidth - paragraph.margin.left - paragraph.margin.right
            int renderStartX = document.margin.left + paragraph.margin.left

            pdfDocument.x = renderStartX

            ParagraphRenderer paragraphRenderer =
                    new ParagraphRenderer(paragraph, pdfDocument, renderStartX, maxLineWidth)

            while (!paragraphRenderer.fullyParsed) {
                paragraphRenderer.parse(pdfDocument.remainingPageHeight)
                paragraphRenderer.render(pdfDocument.y)
                if (paragraphRenderer.fullyParsed) {
                    pdfDocument.scrollDownPage(paragraphRenderer.parsedHeight)
                } else {
                    pdfDocument.addPage()
                }
            }
        }
    }

    def onTableComplete = { Table table ->
        if (renderState == RenderState.PAGE) {
            pdfDocument.x = table.margin.left + document.margin.left

            TableRenderer tableRenderer = new TableRenderer(table, pdfDocument, pdfDocument.x)
            while (!tableRenderer.fullyParsed) {
                tableRenderer.parse(pdfDocument.remainingPageHeight)
                tableRenderer.render(pdfDocument.y)

                if (tableRenderer.fullyParsed) {
                    pdfDocument.scrollDownPage(tableRenderer.parsedHeight)
                } else {
                    pdfDocument.addPage()
                }
            }
        }
    }

    void writeDocument(Document document, OutputStream out) {
        addHeaderFooter()
        addMetadata()

        pdfDocument.contentStream?.close()
        pdfDocument.pdDocument.save(out)
        pdfDocument.pdDocument.close()
    }

    private void addHeaderFooter() {
        int pageCount = pdfDocument.pages.size()
        def options = new HeaderFooterOptions(pageCount: pageCount, dateGenerated: new Date())

        (1..pageCount).each { int pageNumber ->
            pdfDocument.pageNumber = pageNumber
            options.pageNumber = pageNumber

            if (document.header) {
                renderState = RenderState.HEADER
                def header = document.header(options)
                renderHeaderFooter(header)
            }
            if (document.footer) {
                renderState = RenderState.FOOTER
                def footer = document.footer(options)
                renderHeaderFooter(footer)
            }
        }

        renderState = RenderState.PAGE
    }

    private void renderHeaderFooter(headerFooter) {
        float startX = document.margin.left + headerFooter.margin.left
        float startY

        if (renderState == RenderState.HEADER) {
            startY = 0
        } else {
            float pageBottom = pdfDocument.pageBottomY + document.margin.bottom
            startY = pageBottom - getElementHeight(headerFooter) - headerFooter.margin.bottom
        }

        def renderer
        if (headerFooter instanceof TextBlock) {
            renderer = new ParagraphRenderer(headerFooter, pdfDocument, startX, document.width)
        } else {
            renderer = new TableRenderer(headerFooter as Table, pdfDocument, startX)
        }

        renderer.parse(document.height)
        renderer.render(startY)
    }

    private float getElementHeight(element) {
        float width = document.width - document.margin.top - document.margin.bottom

        if (element instanceof TextBlock) {
            new ParagraphRenderer(element, pdfDocument, 0, width).totalHeight
        } else if (element instanceof Table) {
            new TableRenderer(element, pdfDocument, 0).totalHeight
        } else {
            0
        }
    }

    private void addMetadata() {
        ByteArrayOutputStream xmpOut = new ByteArrayOutputStream()
        def xml = new MarkupBuilder(xmpOut.newWriter())

        xml.document(marginTop: "${document.margin.top}", marginBottom: "${document.margin.bottom}",
                marginLeft: "${document.margin.left}", marginRight: "${document.margin.right}") {

            delegate = xml
            resolveStrategy = Closure.DELEGATE_FIRST

            document.children.each { child ->
                switch (child.getClass()) {
                    case TextBlock:
                        addParagraphToMetadata(delegate, child)
                        break
                    case Table:
                        addTableToMetadata(delegate, child)
                        break
                }
            }
        }

        def catalog = pdfDocument.pdDocument.documentCatalog
        InputStream inputStream = new ByteArrayInputStream(xmpOut.toByteArray())

        PDMetadata metadata = new PDMetadata(pdfDocument.pdDocument, inputStream, false)
        catalog.metadata = metadata
    }

    private void addParagraphToMetadata(builder, TextBlock paragraphNode) {
        builder.paragraph(marginTop: "${paragraphNode.margin.top}",
                marginBottom: "${paragraphNode.margin.bottom}",
                marginLeft: "${paragraphNode.margin.left}",
                marginRight: "${paragraphNode.margin.right}") {
            paragraphNode.children?.findAll { it.getClass() == Image }.each {
                builder.image()
            }
        }
    }

    private void addTableToMetadata(builder, Table tableNode) {

        builder.table(columns: tableNode.columnCount, width: tableNode.width, borderSize: tableNode.border.size) {

            delegate = builder
            resolveStrategy = Closure.DELEGATE_FIRST

            tableNode.children.each {
                def cells = it.children
                row {
                    cells.each {
                        cell(width: "${it.width ?: 0}")
                    }
                }
            }
        }
    }

}
