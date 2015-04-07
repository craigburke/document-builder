package com.craigburke.document.builder.render

import com.craigburke.document.core.TextBlock

/**
 * Factory class for ParagraphElements
 * @author Craig Burke
 */
class ParagraphElementBuilder {

    static ParagraphElement buildParagraphElement(TextBlock paragraph, Integer maxWidth) {
        ParagraphElement paragraphElement = new ParagraphElement()
        paragraphElement.node = paragraph
        paragraphElement.lines = ParagraphParser.getLines(paragraphElement, maxWidth)
        paragraphElement
    }

}
