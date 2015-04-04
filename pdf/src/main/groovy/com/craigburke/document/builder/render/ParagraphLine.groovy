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
        if (paragraph.lineHeight) {
            paragraph.lineHeight
        }
        else {
            BigDecimal result = elements.collect {
                (it instanceof ImageElement) ? it.node.height :
                    (it.node.font.size * paragraph.textHeightMultiplier)
            }.max() ?: 0
            Math.ceil(result)
        }
    }

}
