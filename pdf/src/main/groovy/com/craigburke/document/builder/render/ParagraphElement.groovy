package com.craigburke.document.builder.render

import com.craigburke.document.core.Paragraph

/**
 * Rendering element for the Paragraph node
 * @author Craig Burke
 */
class ParagraphElement {

    Paragraph node
    List<ParagraphLine> lines

    ParagraphElement(Paragraph paragraph, Integer maxWidth) {
        node = paragraph
        lines = ParagraphParser.getLines(paragraph, maxWidth)
    }

}
