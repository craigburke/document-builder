package com.craigburke.document.builder.pdf.render

import com.craigburke.document.builder.pdf.PdfDocument
import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class ParagraphRenderer {

    Document document
    Paragraph paragraph
    private final int maxLineWidth
    private List<ParagraphLine> lines = []

    ParagraphRenderer(Paragraph paragraph, Document document) {
        this.paragraph = paragraph
        this.document = document
        maxLineWidth = document.item.currentPage.mediaBox.width - document.margin.left - document.margin.right - paragraph.margin.left - paragraph.margin.right
        parseLines()
    }

    private PdfDocument getPdfDocument() {
        document.item
    }

    private void parseLines() {
        def currentChunk = []
        def paragraphChunks = []
        paragraphChunks << currentChunk

        paragraph.children.each { child ->
            if (child instanceof LineBreak) {
                currentChunk = []
                paragraphChunks << currentChunk
            }
            else {
                currentChunk << child
            }
        }

        paragraphChunks.each { lines += parseParagraphChunk(it) }
    }

    private List<ParagraphLine> parseParagraphChunk(chunk) {
        def chunkLines = []

        ParagraphLine currentLine = new ParagraphLine(maxLineWidth)
        chunkLines << currentLine

        PDFont pdfFont

        chunk.each { node ->
            if (node instanceof Text) {
                Font font = node.font
                pdfFont = PDType1Font.HELVETICA
                String remainingText = node.value

                while (remainingText) {
                    BigDecimal textWidth = pdfFont.getStringWidth(remainingText)  / 1000 * font.size

                    if (currentLine.contentWidth + textWidth > maxLineWidth) {
                        String splitText = getTextUntilBreakpoint(remainingText, pdfFont, font.size, currentLine.remainingWidth)

                        remainingText = remainingText - splitText
                        int elementWidth = pdfFont.getStringWidth(splitText)  / 1000 * font.size
                        currentLine.contentWidth += elementWidth
                        currentLine.elements << new TextElement(pdfFont: pdfFont, text: splitText, node: node, width: elementWidth)

                        currentLine = new ParagraphLine(maxLineWidth)
                        chunkLines << currentLine
                    }
                    else {
                        currentLine.elements << new TextElement(pdfFont: pdfFont, text: remainingText, node: node, width: textWidth)
                        remainingText = ""
                        currentLine.contentWidth += textWidth
                    }

                }
            }
            else {
                if (currentLine.remainingWidth < node.width) {
                    currentLine = new ParagraphLine(maxLineWidth)
                    chunkLines << currentLine
                }

                currentLine.elements << new ImageElement(node: node)
            }
        }

        chunkLines
    }

    private String getTextUntilBreakpoint(String text, PDFont font, BigDecimal fontSize, BigDecimal width) {
        String result = ""
        String previousResult
        boolean spaceBreakpointFound = false

        String[] words = text.split()*.trim()
        int wordIndex = 0
        int resultWidth = 0
        while (words && resultWidth < width && wordIndex < words.size()) {
            previousResult = result
            result += (wordIndex == 0 ? '' : ' ') + words[wordIndex]
            resultWidth = getTextWidth(result, font, fontSize)

            if (resultWidth == width) {
                spaceBreakpointFound = true
                break
            }
            else if (resultWidth < width) {
                spaceBreakpointFound = true
            }
            else if (spaceBreakpointFound) {
                result = previousResult
                break
            }
            wordIndex++
        }

        if (!spaceBreakpointFound) {
            // Fall back to breaking line in the middle of a word
            int currentCharacter = 0
            while (getTextWidth(result, font, fontSize) < width) {
                result += text[currentCharacter]
                currentCharacter++
            }
        }

        result
    }


    private BigDecimal getTextWidth(String text, PDFont font, BigDecimal fontSize) {
        font.getStringWidth(text) / 1000 * fontSize
    }


    void render() {
        lines.each { ParagraphLine line ->
            if (pdfDocument.remainingPageHeight < line.height) {
                pdfDocument.addPage()
            }

            document.item.x = document.margin.left + paragraph.margin.left
            document.item.y += line.height

            line.elements.each { element ->

                switch (element.getClass()) {
                    case TextElement:
                        renderTextElement(element)
                        document.item.x += element.width
                        break
                    case ImageElement:
                        renderImageElement(element)
                        document.item.x += element.node.width
                        break
                }
            }

        }

        document.item.y += paragraph.margin.bottom
    }


    private void renderTextElement(TextElement element) {
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

    private void renderImageElement(ImageElement element) {
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



