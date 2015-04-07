package com.craigburke.document.builder.render

import com.craigburke.document.core.TextBlock

/**
 * Rendering element for a Paragraph node
 * @author Craig Burke
 */
class ParagraphElement {
    TextBlock node
    List<ParagraphLine> lines

    int getTotalHeight() {
        node.margin.top + lines.sum { it.contentHeight + it.lineSpacing } + node.margin.bottom
    }
}
