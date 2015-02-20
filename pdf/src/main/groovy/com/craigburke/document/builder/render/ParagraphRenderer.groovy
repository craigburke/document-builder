package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Document
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class ParagraphRenderer {

    Document document
    Paragraph paragraph
    private final int maxLineWidth
    private final int renderStartX
    private ParagraphElement paragraphElement

    ParagraphRenderer(Paragraph paragraph, Document document, int renderStartX, int maxLineWidth) {
        this.paragraph = paragraph
        this.document = document
        this.renderStartX = renderStartX
        this.maxLineWidth = maxLineWidth
        paragraphElement = new ParagraphElement(paragraph, maxLineWidth)
    }

    void render() {
        paragraphElement.lines.each { ParagraphLine line ->
            ParagraphRenderer.renderLine(document, line, renderStartX)
        }

        document.item.y += paragraph.margin.bottom
    }

    static void renderLine(Document document, ParagraphLine line, int renderStartX) {
        PdfDocument pdfDocument = document.item

        if (pdfDocument.remainingPageHeight < line.height) {
            pdfDocument.addPage()
        }

        pdfDocument.x = renderStartX
        pdfDocument.y += line.height

        line.elements.each { element ->

            switch (element.getClass()) {
                case TextElement:
                    renderTextElement(element, document)
                    pdfDocument.x += element.width
                    break
                case ImageElement:
                    renderImageElement(element, document)
                    pdfDocument.x += element.node.width
                    break
            }
        }
    }

    private static void renderTextElement(TextElement element, Document document) {
        PdfDocument pdfDocument = document.item
        Text text = element.node

        PDPageContentStream contentStream = document.item.contentStream

        contentStream.beginText()
        contentStream.moveTextPositionByAmount(pdfDocument.x, pdfDocument.translatedY)

        def color = text.font.color.RGB
        contentStream.setNonStrokingColor(color[0], color[1], color[2])
        contentStream.setFont(element.pdfFont, text.font.size)
        contentStream.drawString(element.text)

        contentStream.endText()
    }

    private static void renderImageElement(ImageElement element, Document document) {
        PdfDocument pdfDocument = document.item

        InputStream inputStream = new ByteArrayInputStream(element.node.data)
        BufferedImage bufferedImage = ImageIO.read(inputStream)

        PDXObjectImage img
        if (element.node.name.endsWith('png')) {
            img = new PDPixelMap(pdfDocument.pdDocument, bufferedImage)
        }
        else {
            img = new PDJpeg(pdfDocument.pdDocument, bufferedImage)
        }

        img.width = element.node.width
        img.height = element.node.height

        pdfDocument.contentStream.drawImage(img, pdfDocument.x, pdfDocument.translatedY)
    }

}



