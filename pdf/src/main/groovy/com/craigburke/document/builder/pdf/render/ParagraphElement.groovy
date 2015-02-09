package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Paragraph

class ParagraphElement {

    Paragraph node
    List<ParagraphLine> lines

    ParagraphElement(Paragraph paragraph, int maxWidth) {
        node = paragraph
        lines = ParagraphParser.getLines(paragraph, maxWidth)
    }

    int getHeight() {
        node.margin.top + lines.sum { it.height } + node.margin.bottom
    }

}
