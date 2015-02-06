package com.craigburke.document.builder.pdf.render

class ParagraphLine {
    final int maxWidth
    int contentWidth = 0

    ParagraphLine(int maxWidth) {
        this.maxWidth = maxWidth
    }

    int getRemainingWidth() {
        maxWidth - contentWidth
    }

    int getHeight() {
        elements.collect { (it instanceof ImageElement) ? it.node.height : it.node.parent.leading }.max()
    }

    List elements = []
}
