package com.craigburke.document.builder.render

import com.craigburke.document.core.Paragraph

/**
 * A paragraph line
 * @author Craig Burke
 */
class ParagraphLine {
    final int maxWidth
    int contentWidth = 0
    List elements = []
    Paragraph paragraph

    ParagraphLine(Paragraph paragraph, int maxWidth) {
        this.paragraph = paragraph
        this.maxWidth = maxWidth
    }

    int getRemainingWidth() {
        maxWidth - contentWidth
    }

    int getHeight() {
        elements.collect { (it.getClass() == ImageElement) ? it.node.height : paragraph.lineHeight }.max() ?: 0
    }

}
