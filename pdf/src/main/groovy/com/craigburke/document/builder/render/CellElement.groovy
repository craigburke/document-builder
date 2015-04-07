package com.craigburke.document.builder.render

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Table

/**
 * Rendering element for the Cell node
 * @author Craig Burke
 */
class CellElement {

    Cell node
    List<ParagraphElement> paragraphElements = []

    class LinePosition {
        int line
        int element
    }

    private LinePosition position
    boolean fullyRendered = false
    int renderedHeight = 0

    CellElement(Cell cell) {
        this.node = cell
        Table table = cell.parent.parent

        cell.children.each { paragraph ->
            int renderWidth = cell.width - (table.padding * 2)
            paragraphElements << ParagraphElementBuilder.buildParagraphElement(paragraph, renderWidth)
        }
        position = new LinePosition(element:0, line:0)
    }

    int getTotalHeight() {
        paragraphElements.sum { it.totalHeight }
    }

    void moveToNextLine() {
        if (position.line == (currentElement.lines.size() - 1)) {
            if (position.element < (paragraphElements.size() - 1)) {
                position.element++
                position.line = 0
            }
        }
        else {
            position.line++
        }
    }

    void moveToPreviousLine() {
        if (position.line > 0) {
            position.line--
        }
        else if (position.element > 0) {
            position.element--
            position.line = paragraphElements[position.element].lines.size() - 1
        }
    }

    boolean isOnLastLine() {
        (currentElement == paragraphElements?.last() && currentLine == currentElement?.lines?.last())
    }

    boolean isOnFirstLine() {
        (currentElement == paragraphElements?.first() && currentLine == currentElement?.lines?.first())
    }

    ParagraphElement getCurrentElement() {
        paragraphElements[position.element]
    }

    ParagraphLine getCurrentLine() {
        currentElement.lines[position.line]
    }

}

