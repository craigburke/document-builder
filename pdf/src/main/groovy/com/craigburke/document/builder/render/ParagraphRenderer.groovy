package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Align
import com.craigburke.document.core.Document
import com.craigburke.document.core.ImageType
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
        paragraphElement = ParagraphElementBuilder.buildParagraphElement(paragraph, maxLineWidth)
    }

    int getTotalHeight() {
        int height = paragraph.margin.top + paragraph.margin.bottom
        height += paragraphElement.totalHeight
        height
    }

    void render(RenderState renderState = RenderState.PAGE) {
        paragraphElement.lines.each { ParagraphLine line ->
            ParagraphRenderer.renderLine(document, line, renderStartX, renderState)
        }
    }

    static void renderLine(Document document, ParagraphLine line, float renderStartX, RenderState renderState) {
        PdfDocument pdfDocument = document.element

        if (renderState == RenderState.PAGE && pdfDocument.remainingPageHeight < line.lineSpacing) {
            pdfDocument.addPage()
        }

        switch (line.paragraphElement.node.align) {
            case Align.RIGHT:
                renderStartX += line.maxWidth - line.contentWidth
                break
            case Align.CENTER:
                renderStartX += Math.round((line.maxWidth - line.contentWidth) / 2)
        }

        pdfDocument.x = renderStartX
        pdfDocument.y += line.contentHeight

        line.elements.each { element ->
            int offset = 0

            switch (element.getClass()) {
                case TextElement:
                    offset = line.contentHeight - element.node.font.size
                    pdfDocument.y -= offset
                    renderTextElement(element, document)

                    pdfDocument.x += element.width
                    break
                case ImageElement:
                    offset = line.contentHeight - element.node.height
                    pdfDocument.y -= offset
                    renderImageElement(element, document)
                    pdfDocument.x += element.node.width
                    break
            }

            pdfDocument.y += offset
        }
        pdfDocument.y += line.lineSpacing
    }

    private static void renderTextElement(TextElement element, Document document) {
        PdfDocument pdfDocument = document.element
        Text text = element.node

        PDPageContentStream contentStream = document.element.contentStream

        contentStream.beginText()
        contentStream.moveTextPositionByAmount(pdfDocument.x, pdfDocument.translatedY)

        def color = text.font.color.rgb
        contentStream.setNonStrokingColor(color[0], color[1], color[2])
        contentStream.setFont(element.pdfFont, text.font.size)
        contentStream.drawString(element.text)

        contentStream.endText()
    }

    private static void renderImageElement(ImageElement element, Document document) {
        PdfDocument pdfDocument = document.element

        InputStream inputStream = new ByteArrayInputStream(element.node.data)
        BufferedImage bufferedImage = ImageIO.read(inputStream)

        PDXObjectImage img
        if (element.node.type == ImageType.PNG) {
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
