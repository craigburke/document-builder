package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Cell

class CellElement {

    Cell node
    List<ParagraphElement> paragraphElements = []
    private def currentPosition = [element: 0, line: 0]
    boolean fullyRendered = false

    CellElement(Cell cell) {
        this.node = cell
        cell.children.each { paragraph ->
            paragraphElements << new ParagraphElement(paragraph, cell.width.intValue())
        }
    }

    void moveToNextLine() {
        if (currentPosition.line == currentElement.lines.size() - 1) {
            currentPosition.element++
            currentPosition.line = 0
        }
        else {
            currentPosition.line++
        }
    }

    void moveToPreviousLine() {
        if (currentPosition.line > 0) {
            currentPosition.line--
        }
        else if (currentPosition.element > 0) {
            currentPosition.element--
            currentPosition.line = paragraphElements[currentPosition.element].lines.size() - 1
        }
    }

    boolean isOnLastLine() {
        (currentElement == paragraphElements.last())
    }

    int getHeight() {
        paragraphElements.sum { it.height }
    }

    ParagraphElement getCurrentElement() {
        paragraphElements[currentPosition.element]
    }

    ParagraphLine getCurrentLine() {
        currentElement.lines[currentPosition.line]
    }

}
