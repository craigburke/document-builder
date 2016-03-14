package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Align
import com.craigburke.document.core.ImageType
import com.craigburke.document.core.Text
import com.craigburke.document.core.TextBlock
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

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

    float renderedHeight = 0
    private float startX
    private float totalWidth
    private boolean parsedAndRendered = false
    private boolean fullyRendered = false
    private boolean fullyParsed = false

    ParagraphRenderer(TextBlock paragraph, PdfDocument pdfDocument, float startX, float totalWidth) {
        node = paragraph
        this.pdfDocument = pdfDocument
        this.startX = startX
        this.totalWidth = totalWidth
        lines = ParagraphParser.getLines(paragraph, totalWidth)
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
        if (!lines || fullyRendered) {
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

        renderBackground()

        lines[parseStart..parseEnd].each { ParagraphLine line ->
            pdfDocument.x = startX
            renderLine(line)
        }
        renderedHeight = parsedHeight
        fullyRendered = fullyParsed
        parsedAndRendered = true
    }

    void renderBackground() {
        if (!node.background) {
            return
        }
        PDPageContentStream contentStream = pdfDocument.contentStream

        float height = parsedHeight
        float backgroundBottomY = pdfDocument.translateY(pdfDocument.y + parsedHeight)

        contentStream.setNonStrokingColor(*node.background.rgb)
        contentStream.addRect(startX, backgroundBottomY, totalWidth, height)
        contentStream.fill()

    }

    float getTotalHeight() {
        lines.sum { it.totalHeight } as float
    }

    float getParsedHeight() {
        if (!linesParsed) {
            return 0f
        }

        lines[parseStart..parseEnd]*.totalHeight.sum() as float ?: 0f
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
            if (element instanceof TextElement) {
                renderTextElement(element as TextElement, line)
                pdfDocument.x += element.width
            }
            else if (element instanceof ImageElement) {
                renderImageElement(element as ImageElement)
                pdfDocument.x += element.node.width
            }
        }

        pdfDocument.y += line.lineSpacing
    }

    private void renderTextElement(TextElement element, ParagraphLine line) {
        Text text = element.node

        PDPageContentStream contentStream = pdfDocument.contentStream
        float startX = pdfDocument.x
        float startY = pdfDocument.translatedY

        if (text.background) {
            float height = line.contentHeight + line.lineSpacing
            float backgroundBottomY = pdfDocument.translateY(pdfDocument.y + line.contentHeight - line.lineSpacing)
            contentStream.setNonStrokingColor(*text.background.rgb)
            contentStream.addRect(startX, backgroundBottomY, element.width, height)
            contentStream.fill()
        }

        contentStream.beginText()
        contentStream.newLineAtOffset(startX, startY)

        def color = text.font.color.rgb
        contentStream.setNonStrokingColor(color[0], color[1], color[2])
        contentStream.setFont(element.pdfFont, text.font.size)
        contentStream.showText(element.text)

        contentStream.endText()
    }

    private void renderImageElement(ImageElement element) {
        InputStream inputStream = new ByteArrayInputStream(element.node.data)
        BufferedImage bufferedImage = ImageIO.read(inputStream)

        PDImageXObject img
        if (element.node.type == ImageType.PNG) {
            img = LosslessFactory.createFromImage(pdfDocument.pdDocument, bufferedImage)
        }
        else {
            img = JPEGFactory.createFromImage(pdfDocument.pdDocument, bufferedImage)
        }

        int width = element.node.width
        int height = element.node.height

        pdfDocument.contentStream.drawImage(img, pdfDocument.x, pdfDocument.translatedY, width, height)
    }

}
