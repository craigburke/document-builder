package com.craigburke.document.builder

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.Text
import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.util.TextPosition

// This extractor has serious limitations but works for simple tests
// It can't split text reliably when a paragraph has no top/bottom margins
class PdfContentExtractor extends PDFTextStripper {

        private def tablePosition = [row: 0, cell: 0]
        private int currentChildNumber = 0
        private Document document
        private TextPosition lastPosition

        PdfContentExtractor(Document document) {
            super.setSortByPosition( true )
            this.document = document
        }

        private def getCurrentChild() {
            if (!document.children || document.children.size() < currentChildNumber) {
                null
            }
            else {
                document.children[currentChildNumber - 1]
            }
        }

        @Override
        void processTextPosition( TextPosition text )  {
            if (text.character == ' ') {
                return
            }
            
            updateChildNumber(text)

            def currentFont = new Font(family: text.font.baseFont, size: text.fontSizeInPt)
            def textNode

            if (currentChild instanceof Paragraph) {
                textNode = processParagraph(text, currentFont)
            }
            else {
                textNode = processTable(text, currentFont)
            }

            textNode?.value += text.character
            lastPosition = text
        }

        private processTable(TextPosition text, Font font ) {
            def textNode
            Cell cell = currentChild.rows[tablePosition.row].cells[tablePosition.cell]

            if (!cell.children || isNewSection(text)) {
                textNode = new Text(value: '', font: font)
                cell.children << textNode
            }
            else {
                textNode = cell.children.last()
            }

            textNode
        }

        private processParagraph(TextPosition text, Font font ) {
            def textNode

            if (!currentChild.children) {
                textNode = createText(currentChild, font)
                currentChild.children << textNode
                setParagraphProperties(currentChild, text, font)
            }
            else if (isNewSection(text)) {
                textNode = createText(currentChild, font)
                currentChild.children << textNode
            }
            else {
                textNode = currentChild.children.last()
            }

            textNode
        }

        private void setParagraphProperties(paragraph, TextPosition text, Font font) {
            paragraph.font = font
            paragraph.margin.left = text.x - document.margin.left
            paragraph.margin.right = text.pageWidth - text.width - paragraph.margin.left - document.margin.right - document.margin.left

            paragraph.margin.top = Math.round(text.y - document.margin.top - paragraph.leading)
        }

        private Text createText(paragraph, Font font) {
            new Text(parent: paragraph, value: '', font: font)
        }

        private void updateChildNumber(TextPosition current) {
            if (!lastPosition || (lastPosition.y != current.y && current.character != ' ')) {
                currentChildNumber++
                tablePosition = [row: 0, cell: 0]
            }
        }

        private boolean isNewSection(TextPosition current) {
            boolean isNewSection = false

            if (!lastPosition) {
                isNewSection = true
            }
            else if (current.font.baseFont != lastPosition.font.baseFont) {
                isNewSection = true
            }
            else if (current.fontSizeInPt != lastPosition.fontSizeInPt) {
                isNewSection = true
            }

            isNewSection
        }


}
