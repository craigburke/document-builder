package com.craigburke.document.builder.render

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Document
import com.craigburke.document.core.Table
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.builder.RenderState

/**
 * Rendering element for the Cell node
 * @author Craig Burke
 */
class CellElement implements Renderable {
    float startX
    Cell node
    List<Renderable> childElements = []

    CellElement(Cell cell, float startX) {
        this.node = cell
        this.startX = startX
        Table table = cell.parent.parent
        int renderWidth = cell.width - (table.padding * 2)

        cell.children.each { child ->
            if (child instanceof TextBlock) {
                childElements << new ParagraphElement(child, startX, renderWidth)
            }
            else if (child instanceof Table) {
                childElements << new TableElement(child, startX)
            }
        }
    }

    boolean isFullyParsed() {
        childElements.every { it.fullyParsed }
    }

    float getTotalHeight() {
        childElements.max { it.totalHeight }.totalHeight
    }

    float getParsedHeight() {
        childElements.max { it.parsedHeight }.parsedHeight
    }

    void render(Document document, RenderState renderState) {
        childElements*.render(document, renderState)
    }

    void parseUntilHeight(float height) {
        childElements*.parseUntilHeight(height)
    }
}

