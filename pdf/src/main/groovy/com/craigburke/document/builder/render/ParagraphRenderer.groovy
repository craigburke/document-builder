package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Align
import com.craigburke.document.core.Document
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Text
import com.craigburke.document.core.builder.RenderState
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Renders a paragraph to the document
 * @author Craig Burke
 */
class ParagraphRenderer {

    Document document
    TextBlock paragraph
    private final int maxLineWidth
    private final int renderStartX
    private ParagraphElement paragraphElement

    ParagraphRenderer(TextBlock paragraph, Document document, int renderStartX, int maxLineWidth) {
        this.paragraph = paragraph
        this.document = document
        this.renderStartX = renderStartX
        this.maxLineWidth = maxLineWidth
        paragraphElement = new ParagraphElement(paragraph, maxLineWidth)
    }

    void render(RenderState renderState = RenderState.PAGE) {
        paragraphElement.lines.each { ParagraphLine line ->
            ParagraphRenderer.renderLine(document, line, renderStartX, renderState)
        }
    }

    static void renderLine(Document document, ParagraphLine line, int renderStartX, RenderState renderState) {
        PdfDocument pdfDocument = document.item

        if (renderState == RenderState.PAGE && pdfDocument.remainingPageHeight < line.height) {
            pdfDocument.addPage()
        }

        switch (line.paragraph.align) {
            case Align.RIGHT:
                renderStartX += line.maxWidth - line.contentWidth
                break
            case Align.CENTER:
                renderStartX += Math.round((line.maxWidth - line.contentWidth) / 2)
        }

        pdfDocument.x = renderStartX
        pdfDocument.y += line.height
        if (line.height == line.paragraph.textHeight) {
            pdfDocument.y += line.paragraph.textHeightOffset
        }

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

        if (line.height == line.paragraph.textHeight) {
            pdfDocument.y -= line.paragraph.textHeightOffset
        }
    }

    private static void renderTextElement(TextElement element, Document document) {
        PdfDocument pdfDocument = document.item
        Text text = element.node

        PDPageContentStream contentStream = document.item.contentStream

        contentStream.beginText()
        contentStream.moveTextPositionByAmount(pdfDocument.x, pdfDocument.translatedY)

        def color = text.font.color.rgb
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

        int width = element.node.width
        int height = element.node.height

        pdfDocument.contentStream.drawXObject(img, pdfDocument.x, pdfDocument.translatedY, width, height)
    }

}
