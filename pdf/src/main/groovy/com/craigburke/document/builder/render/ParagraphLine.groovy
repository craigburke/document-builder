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
        elements.collect { (it.getClass() == ImageElement) ? it.node.height : paragraph.textHeight }.max() ?: 0
    }

}
