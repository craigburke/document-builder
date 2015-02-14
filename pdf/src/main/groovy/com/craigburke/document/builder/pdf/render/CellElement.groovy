package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Cell

class CellElement {

    Cell node
    List<ParagraphElement> paragraphElements = []

    private LinePosition position
    boolean fullyRendered = false
    int renderedHeight = 0

    CellElement(Cell cell) {
        this.node = cell
        cell.children.each { paragraph ->
            paragraphElements << new ParagraphElement(paragraph, cell.width)
        }
        position = new LinePosition(element: 0, line: 0)
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
        (currentElement == paragraphElements.last() && currentLine == currentElement.lines.last())
    }

    ParagraphElement getCurrentElement() {
        paragraphElements[position.element]
    }

    ParagraphLine getCurrentLine() {
        currentElement.lines[position.line]
    }

}

class LinePosition {
    int line
    int element
}
