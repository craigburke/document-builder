package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Paragraph

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
        elements.collect { (it instanceof ImageElement) ? it.node.height : paragraph.leading }.max() ?: 0
    }

}
