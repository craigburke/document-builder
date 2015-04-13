package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Align
import com.craigburke.document.core.Document
import com.craigburke.document.core.ImageType
import com.craigburke.document.core.Text
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.builder.RenderState
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Rendering element for a Paragraph node
 * @author Craig Burke
 */
class ParagraphElement implements Renderable {
    TextBlock node
    List<ParagraphLine> lines
    private int positionStart = 0
    private int positionEnd = 0
    private float startX

    ParagraphElement(TextBlock paragraph, float startX, float maxWidth) {
        node = paragraph
        this.startX = startX
        lines = ParagraphParser.getLines(paragraph, maxWidth)
    }

    void parseUntilHeight(float height) {
        if (!lines) {
            fullyParsed = true
            return
        }

        boolean reachedEnd = false
        float parsedHeight = 0

        while (!reachedEnd) {
            ParagraphLine line = lines[positionEnd]
            parsedHeight += line.totalHeight

            if (parsedHeight > height) {
                positionEnd = (positionEnd == 1) ? 1 : positionEnd--
                reachedEnd = true
                fullyParsed = false
            }
            else if (line == lines.last()) {
                reachedEnd = true
                fullyParsed = true
            }
            else {
                positionEnd++
            }
        }
    }

    void render(Document document, RenderState renderState) {
        lines[positionStart..positionEnd].each { ParagraphLine line ->
            renderLine(document, line, renderState)
        }
        positionStart = positionEnd
    }

    float getTotalHeight() {
        node.margin.top + lines.sum { it.contentHeight + it.lineSpacing } + node.margin.bottom
    }

    float getParsedHeight() {
        float linesHeight = lines[positionStart..positionEnd].sum { it.totalHeight }
        node.margin.top + linesHeight + (fullyParsed ? node.margin.bottom : 0)
    }

    private void renderLine(Document document, ParagraphLine line, RenderState renderState) {
        PdfDocument pdfDocument = document.element
        float renderStartX = startX

        if (renderState == RenderState.PAGE && pdfDocument.remainingPageHeight < line.totalHeight) {
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
