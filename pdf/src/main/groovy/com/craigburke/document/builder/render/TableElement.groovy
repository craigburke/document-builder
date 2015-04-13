package com.craigburke.document.builder.render

import com.craigburke.document.core.Document
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.builder.RenderState

/**
 * Rendering element for a Table node
 * @author Craig Burke
 */
class TableElement implements Renderable {

    Table node
    List<RowElement> rowElements = []

    TableElement(Table table, float startX) {
        node = table
        table.children.each { Row row ->
            rowElements << new RowElement(row, startX)
        }
    }

    void parseUntilHeight(float height) {
        rowElements*.parseUntilHeight(height)
    }

    boolean isFullyParsed() {
        rowElements.each { it.fullyParsed }
    }

    float getTotalHeight() {
        rowElements.max { it.totalHeight }.totalHeight
    }

    float getParsedHeight() {
        rowElements.max { it.parsedHeight }.parsedHeight
    }

    void render(Document document, RenderState renderState) {
        rowElements*.render(document, renderState)
    }

}
