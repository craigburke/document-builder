package com.craigburke.document.builder.pdf

import com.craigburke.document.builder.pdf.render.CellElement
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Font
import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.Text
import spock.lang.Shared
import spock.lang.Specification

class CellElementSpec extends Specification {

    @Shared CellElement cellElement
    @Shared int totalLineCount


    def setup() {
        Table table = new Table()
        Row row = new Row(parent: table)
        def cell = new Cell(width: 72, parent: row)
        5.times { i ->
            def paragraph = new Paragraph()
            i.times {
                paragraph.children << new Text(value: "FOO${i}", font: new Font(family: 'Helvetica', size: 12))
                paragraph.children << new LineBreak()
            }
            cell.children << paragraph
        }
        cellElement = new CellElement(cell)
    }

    def "onLastLine returns true when on last line"() {
        given:
        cellElement.onLastLine == false

        when:
        13.times {
            cellElement.moveToNextLine()
            assert cellElement.onLastLine == false
        }

        cellElement.moveToNextLine()

        then:
        cellElement.onLastLine == true

        when:
        cellElement.moveToNextLine()

        then:
        cellElement.onLastLine == true
    }


    def "moveToNextLine and moveToPrevious line allow you to change position"() {
        when:
        while (!cellElement.onLastLine) {
            cellElement.moveToNextLine()
        }

        then:
        cellElement.position.element == 4
        cellElement.position.line == 4

        when:
        cellElement.moveToPreviousLine()

        then:
        cellElement.position.element == 4
        cellElement.position.line == 3

        when:
        4.times {
            cellElement.moveToPreviousLine()
        }

        then:
        cellElement.position.element == 3
        cellElement.position.line == 3

        when:
        4.times {
            cellElement.moveToPreviousLine()
        }

        then:
        cellElement.position.element == 2
        cellElement.position.line == 2

        when:
        3.times {
            cellElement.moveToPreviousLine()
        }

        then:
        cellElement.position.element == 1
        cellElement.position.line == 1

        when:
        2.times {
            cellElement.moveToPreviousLine()
        }

        then:
        cellElement.position.element == 0
        cellElement.position.line == 0

        when:
        cellElement.moveToPreviousLine()

        then:
        cellElement.position.element == 0
        cellElement.position.line == 0
    }


}
