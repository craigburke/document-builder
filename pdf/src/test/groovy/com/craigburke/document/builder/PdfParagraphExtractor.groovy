package com.craigburke.document.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.util.TextPosition

// This extractor has serious limitations.
// Can't split text reliably when a paragraph has no top/bottom margins
// Margins for all paragraphs assumed to be consistent
// Works with one line paragraphs only
class PdfParagraphExtractor extends PDFTextStripper {

        private List<Paragraph> _paragraphs = []

        private Document document
        private TextPosition lastPosition

        PdfParagraphExtractor(Document document) {
            super.setSortByPosition( true )
            this.document = document
        }

        def getParagraphs() {
            _paragraphs.findAll { it.text != ' ' }
        }

        void processTextPosition( TextPosition text )  {
            def font = new Font(family: text.font.baseFont, size: text.fontSizeInPt)

            if (isNewParagraph(text)) {
                def paragraph = createParagraph(text, font)

                def textNode = createText(paragraph, font)
                paragraph.children << textNode
                _paragraphs << paragraph
            }
            else if (isNewSection(text)) {
                def paragraph = paragraphs.last()
                def textNode = createText(paragraph, font)
                paragraph.children << textNode
            }

            paragraphs.last().children.last().value += text.character
            lastPosition = text
        }

        private Paragraph createParagraph(TextPosition text, Font font) {
            def paragraph = new Paragraph(parent: document.item, font: font)

            if (!paragraphs) {
                paragraph.marginLeft = text.x - document.marginLeft
                paragraph.marginRight = text.pageWidth - text.width - paragraph.marginLeft - document.marginRight - document.marginLeft
                paragraph.marginTop = text.y - document.marginTop
            }
            else {
                Paragraph firstParagraph = paragraphs.first()
                paragraph.marginLeft = firstParagraph.marginLeft
                paragraph.marginRight = firstParagraph.marginRight
                paragraph.marginTop = firstParagraph.marginTop
                paragraph.marginBottom = firstParagraph.marginBottom
            }

            paragraph
        }

        private Text createText(Paragraph paragraph, Font font) {
            new Text(parent: paragraph, value: '', font: font)
        }

        private boolean isNewParagraph(TextPosition current) {
            (!_paragraphs || lastPosition.y != current.y)
        }

        private boolean isNewSection(TextPosition current) {
            boolean isNewSection = false

            if (current.font.baseFont != lastPosition.font.baseFont) {
                isNewSection = true
            }
            if (current.fontSizeInPt != lastPosition.fontSizeInPt) {
                isNewSection = true
            }

            isNewSection
        }


}
