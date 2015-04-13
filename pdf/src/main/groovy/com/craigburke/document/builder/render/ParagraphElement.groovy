package com.craigburke.document.builder.render

import com.craigburke.document.core.Document
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.builder.RenderState

/**
 * Rendering element for a Paragraph node
 * @author Craig Burke
 */
class ParagraphElement implements Renderable {
    TextBlock node
    List<ParagraphLine> lines
    private int positionStart = 0
    private int positionEnd = 0
    private float startX

    ParagraphElement(TextBlock paragraph, float startX, float maxWidth) {
        node = paragraph
        this.startX = startX
        lines = ParagraphParser.getLines(paragraph, maxWidth)
    }

    void parseUntilHeight(float height) {
        if (!lines) {
            fullyParsed = true
            return
        }

        boolean reachedEnd = false
        float parsedHeight = 0

        while (!reachedEnd) {
            ParagraphLine line = lines[positionEnd]
            parsedHeight += line.totalHeight

            if (parsedHeight > height) {
                positionEnd = (positionEnd == 1) ? 1 : positionEnd--
                reachedEnd = true
                fullyParsed = false
            }
            else if (line == lines.last()) {
                reachedEnd = true
                fullyParsed = true
            }
            else {
                positionEnd++
            }
        }
    }

    void render(Document document, RenderState renderState) {
        lines[positionStart..positionEnd].each { ParagraphLine line ->
            ParagraphRenderer.renderLine(document, line, startX, renderState)
        }
        positionStart = positionEnd
    }

    float getTotalHeight() {
        node.margin.top + lines.sum { it.contentHeight + it.lineSpacing } + node.margin.bottom
    }

    float getParsedHeight() {
        float linesHeight = lines[positionStart..positionEnd].sum { it.totalHeight }
        node.margin.top + linesHeight + (fullyParsed ? node.margin.bottom : 0)
    }

}
