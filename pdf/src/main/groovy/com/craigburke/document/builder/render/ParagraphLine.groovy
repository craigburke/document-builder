package com.craigburke.document.builder.render

import com.craigburke.document.core.TextBlock

/**
 * A paragraph line
 * @author Craig Burke
 */
class ParagraphLine {
    final int maxWidth
    int contentWidth = 0
    List elements = []
    TextBlock paragraph

    ParagraphLine(TextBlock paragraph, int maxWidth) {
        this.paragraph = paragraph
        this.maxWidth = maxWidth
    }

    int getRemainingWidth() {
        maxWidth - contentWidth
    }

    int getHeight() {
        if (paragraph.lineSpacing) {
            paragraph.lineSpacing
        }
        else {
            BigDecimal maxFontSize = elements
                    .findAll { it instanceof TextElement }
                    .collect { it.node.font.size }
                    .max() ?: paragraph.font.size
           Math.ceil(maxFontSize * paragraph.lineSpacingMultiplier)
        }
    }

}
