package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Paragraph

class ParagraphElement {

    Paragraph node
    List<ParagraphLine> lines

    ParagraphElement(Paragraph paragraph, Integer maxWidth) {
        node = paragraph
        lines = ParagraphParser.getLines(paragraph, maxWidth)
    }


}
