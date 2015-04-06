package com.craigburke.document.builder.render

import com.craigburke.document.core.TextBlock

/**
 * Rendering element for the Paragraph node
 * @author Craig Burke
 */
class ParagraphElement {

    TextBlock node
    List<ParagraphLine> lines

    ParagraphElement(TextBlock paragraph, Integer maxWidth) {
        node = paragraph
        lines = ParagraphParser.getLines(paragraph, maxWidth)
    }

    int getTotalHeight() {
        node.margin.top + lines.sum { it.contentHeight + it.lineSpacing } + node.margin.bottom
    }

}
