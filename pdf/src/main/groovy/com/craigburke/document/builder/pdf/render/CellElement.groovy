package com.craigburke.document.builder.pdf.render

import com.craigburke.document.core.Cell

class CellElement {

    ParagraphElement currentElement
    ParagraphLine currentLine
    Cell node
    List<ParagraphElement> paragraphElements

    CellElement(Cell cell) {
        this.node = cell
        cell.children.each { paragraph ->
            paragraphElements << new ParagraphElement(paragraph, cell.width)
        }
    }

    ParagraphLine getNextLine() {

    }

    boolean isFullyRendered() {
        (currentElement == paragraphElements.last() && currentElement?.lines?.size() == currentLine)
    }

}
