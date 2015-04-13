package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfFont
import com.craigburke.document.core.Font
import com.craigburke.document.core.TextBlock

/**
 * A paragraph line
 * @author Craig Burke
 */
class ParagraphLine {

    TextBlock paragraph
    final float maxWidth
    float contentWidth = 0
    List<Renderable> elements = []

    ParagraphLine(TextBlock paragraph, float maxWidth) {
        this.paragraph = paragraph
        this.maxWidth = maxWidth
    }

    int getRemainingWidth() {
        maxWidth - contentWidth
    }

    int getContentHeight() {
        elements.collect {
            if (it instanceof TextElement) { it.node.font.size }
            else if (it instanceof ImageElement ) { it.node.height }
            else { 0 }
        }.max() ?: 0
    }

    int getTotalHeight() {
        contentHeight + lineSpacing
    }

    int getLineSpacing() {
        if (paragraph.lineSpacing) {
            paragraph.lineSpacing
        }
        else {
            Font maxFont = elements
                    .findAll { it instanceof TextElement }
                    .max { it.node.font.size }?.node?.font ?: paragraph.font

            BigDecimal xHeight = PdfFont.getXHeight(maxFont)
            Math.round((maxFont.size - xHeight) * paragraph.lineSpacingMultiplier)
        }
    }

}
