package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Align
import com.craigburke.document.core.ImageType
import com.craigburke.document.core.Text
import com.craigburke.document.core.TextBlock
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
class ParagraphRenderer implements Renderable {
    TextBlock node

    List<ParagraphLine> lines

    private int parseStart = 0
    private int linesParsed = 0

    private float startX
    private boolean parsedAndRendered = false
    private boolean fullyRendered = false
    private boolean fullyParsed = false

    ParagraphRenderer(TextBlock paragraph, PdfDocument pdfDocument, float startX, float maxWidth) {
        node = paragraph
        this.pdfDocument = pdfDocument
        this.startX = startX
        lines = ParagraphParser.getLines(paragraph, maxWidth)
    }

    boolean getFullyParsed() {
        this.fullyParsed
    }

    int getParseStart() {
        this.parseStart
    }

    int getParseEnd() {
        int parseEnd = Math.max(0f, (parseStart + linesParsed - 1))
        Math.min(lines.size() - 1, parseEnd)
    }

    int getLinesParsed() {
        this.linesParsed
    }

    void parse(float height) {
        if (!lines || fullyRendered || !node.text) {
            fullyParsed = true
            return
        }

        if (parsedAndRendered) {
            parseStart += linesParsed
            parseStart = Math.min(lines.size() - 1, parseStart)
        }
        linesParsed = 0

        boolean reachedEnd = false
        float parsedHeight = 0

        while (!reachedEnd) {
            ParagraphLine line = lines[parseStart + linesParsed]
            parsedHeight += line.totalHeight
            linesParsed++

            if (parsedHeight > height) {
                linesParsed = Math.max(0f, linesParsed - 1)
                reachedEnd = true
                fullyParsed = false
            }
            else if (line == lines.last()) {
                reachedEnd = true
                fullyParsed = true
            }
        }
        parsedAndRendered = false
    }

    void renderElement(float startY) {
        if (fullyRendered || !linesParsed) {
            return
        }

        lines[parseStart..parseEnd].each { ParagraphLine line ->
            pdfDocument.x = startX
            renderLine(line)
        }
        fullyRendered = fullyParsed
        parsedAndRendered = true
    }

    float getTotalHeight() {
        lines.sum { it.totalHeight }
    }

    float getParsedHeight() {
        if (!linesParsed || !node.text) {
            return 0
        }

        lines[parseStart..parseEnd]*.totalHeight.sum() ?: 0
    }

    private void renderLine(ParagraphLine line) {
        float renderStartX = startX

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
            switch (element.getClass()) {
                case TextElement:
                    renderTextElement(element)
                    pdfDocument.x += element.width
                    break
                case ImageElement:
                    renderImageElement(element)
                    pdfDocument.x += element.node.width
                    break
            }
        }
        pdfDocument.y += line.lineSpacing
    }

    private void renderTextElement(TextElement element) {
        Text text = element.node

        PDPageContentStream contentStream = pdfDocument.contentStream

        contentStream.beginText()
        contentStream.moveTextPositionByAmount(pdfDocument.x, pdfDocument.translatedY)

        def color = text.font.color.rgb
        contentStream.setNonStrokingColor(color[0], color[1], color[2])
        contentStream.setFont(element.pdfFont, text.font.size)
        contentStream.drawString(element.text)

        contentStream.endText()
    }

    private void renderImageElement(ImageElement element) {
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
