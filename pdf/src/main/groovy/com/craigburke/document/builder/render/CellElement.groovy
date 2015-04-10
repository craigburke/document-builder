package com.craigburke.document.builder.render

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Table
import groovy.transform.AutoClone

/**
 * Rendering element for the Cell node
 * @author Craig Burke
 */
class CellElement {

    float startX
    Cell node
    List<ParagraphElement> paragraphElements = []

    @AutoClone
    class LinePosition {
        int line
        int element
    }

    private LinePosition startPosition
    private LinePosition endPosition

    boolean fullyRendered = false
    float currentHeight = 0

    CellElement(Cell cell, float startX) {
        this.node = cell
        this.startX = startX
        Table table = cell.parent.parent

        cell.children.each { paragraph ->
            int renderWidth = cell.width - (table.padding * 2)
            paragraphElements << ParagraphElementBuilder.buildParagraphElement(paragraph, renderWidth)
        }
        startPosition = new LinePosition(element:0, line:0)
        endPosition = startPosition
    }

    float getTotalHeight() {
        paragraphElements.sum { it.totalHeight }
    }

    void parseUntilHeight(float height) {
        boolean reachedEnd = false

        currentHeight = 0
        startPosition = endPosition.clone()

        while (!reachedEnd) {
            Table table = node.parent.parent

            float previousHeight = currentHeight
            ParagraphLine line = getParagraphLine(endPosition)
            currentHeight += line.contentHeight + line.lineSpacing

            if (onFirstLine(endPosition)) {
                currentHeight += table.padding + table.border.size
            }
            if (onLastLine(endPosition)) {
                currentHeight += table.padding + table.border.size
                reachedEnd = true
            }

            if (currentHeight == height) {
                reachedEnd = true
            }
            else if (currentHeight > height) {
                currentHeight = previousHeight
                moveToPreviousLine(endPosition)
                reachedEnd = true
            }

            if (!reachedEnd) {
                moveToNextLine(endPosition)
            }
        }
    }

    List<ParagraphLine> getCurrentLines() {
        List<ParagraphLine> result = []
        LinePosition tempPosition = startPosition.clone()
        boolean lastElementFound = false

        while (!lastElementFound) {
            ParagraphLine currentLine = getParagraphLine(tempPosition)
            result << currentLine
            if (currentLine == getParagraphLine(endPosition)) {
                lastElementFound = true
            }
            else {
                moveToNextLine(tempPosition)
            }
        }

        result
    }

    void markCurrentLinesRendered() {
        if (onLastLine(endPosition)) {
            fullyRendered = true
        }
    }

    private void moveToNextLine(LinePosition position) {
        ParagraphElement currentElement = paragraphElements[position.element]

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

    private void moveToPreviousLine(LinePosition position) {
        if (position.line > 0) {
            position.line--
        }
        else if (position.element > 0) {
            position.element--
            position.line = paragraphElements[position.element].lines.size() - 1
        }
    }

    private ParagraphElement getParagraphElement(LinePosition position) {
        paragraphElements[position.element]
    }

    private ParagraphLine getParagraphLine(LinePosition position) {
        ParagraphElement currentElement = getParagraphElement(position)
        currentElement.lines[position.line]
    }

    boolean onFirstLine(LinePosition position) {
        ParagraphElement currentElement = getParagraphElement(position)
        ParagraphLine currentLine = getParagraphLine(position)
        (currentElement == paragraphElements?.last() && currentLine == currentElement?.lines?.last())
    }

    boolean onLastLine(LinePosition position) {
        ParagraphElement currentElement = getParagraphElement(position)
        ParagraphLine currentLine = getParagraphLine(position)
        (currentElement == paragraphElements?.last() && currentLine == currentElement?.lines?.last())
    }

}

