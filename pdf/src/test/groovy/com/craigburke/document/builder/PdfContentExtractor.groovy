package com.craigburke.document.builder

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Text
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition

/**
 * Extract the content from a pdf file from paragraphs and tables. There are limitations but works for simple tests
 * It can't split text reliably when a paragraph has no top/bottom margins
 * @author Craig Burke
 */
class PdfContentExtractor extends PDFTextStripper {

    private tablePosition = [row: 0, cell: 0]
    private int currentChildNumber = 0
    private Document document
    private TextPosition lastPosition

    PdfContentExtractor(Document document) {
        super.setSortByPosition(true)
        this.document = document
    }

    private getCurrentChild() {
        if (!document.children || document.children.size() < currentChildNumber) {
            null
        } else {
            document.children[currentChildNumber - 1]
        }
    }

    @Override
    void processTextPosition(TextPosition text) {
        if (text.unicode == ' ') {
            return
        }

        updateChildNumber(text)

        Font currentFont = new Font(family: text.font.baseFont, size: text.fontSize)
        def textNode

        if (currentChild.getClass() == TextBlock) {
            textNode = processParagraph(text, currentFont)
        } else {
            textNode = processTable(text, currentFont)
        }

        textNode?.value += text.unicode
        lastPosition = text
    }

    private processTable(TextPosition text, Font font) {
        def textNode

        Cell cell = currentChild.children[tablePosition.row].children[tablePosition.cell]
        TextBlock paragraph = cell.children[0]
        paragraph.font = paragraph.font ?: font.clone()

        if (!paragraph.children || isNewSection(text)) {
            textNode = getText(paragraph, font)
            paragraph.children << textNode
        } else {
            textNode = paragraph.children.last()
        }

        textNode
    }

    private processParagraph(TextPosition text, Font font) {
        def textNode

        if (!currentChild.children) {
            textNode = getText(currentChild, font)
            currentChild.children << textNode
            setParagraphProperties(currentChild, text, font)
        } else if (isNewSection(text)) {
            textNode = getText(currentChild, font)
            currentChild.children << textNode
        } else {
            textNode = currentChild.children.last()
        }

        textNode
    }

    private void setParagraphProperties(paragraph, TextPosition text, Font font) {
        paragraph.font = font.clone()
        paragraph.margin.left = text.x - document.margin.left
        int totalPageWidth = text.pageWidth - document.margin.right - document.margin.left
        paragraph.margin.right = totalPageWidth - text.width - paragraph.margin.left

        BigDecimal lineHeight = font.size + (font.size * paragraph.lineSpacingMultiplier)
        BigDecimal textOffset = lineHeight - font.size

        int topMargin = Math.ceil(text.y - document.margin.top - lineHeight + textOffset)
        paragraph.margin.top = Math.round(topMargin)
    }

    private Text getText(paragraph, Font font) {
        new Text(parent: paragraph, value: '', font: font.clone())
    }

    private void updateChildNumber(TextPosition current) {
        if (!lastPosition || (lastPosition.y != current.y && current.unicode != ' ')) {
            currentChildNumber++
            tablePosition.row = 0
            tablePosition.cell = 0
        }
    }

    private boolean isNewSection(TextPosition current) {
        boolean isNewSection = false

        if (!lastPosition) {
            isNewSection = true
        } else if (current.font.baseFont != lastPosition.font.baseFont) {
            isNewSection = true
        } else if (current.fontSizeInPt != lastPosition.fontSizeInPt) {
            isNewSection = true
        }

        isNewSection
    }

}
