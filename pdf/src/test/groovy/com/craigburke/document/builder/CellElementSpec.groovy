package com.craigburke.document.builder

import com.craigburke.document.builder.render.CellElement
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Font
import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import com.craigburke.document.core.Text
import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests for CellElement
 * @author Craig Burke
 */
class CellElementSpec extends Specification {

    @Shared CellElement cellElement

    def setup() {
        Table table = new Table(padding: 0, border: [size: 0])
        Row row = new Row(parent:table)
        def cell = new Cell(width:72, parent:row)
        def paragraph = new TextBlock(font:new Font(family:'Helvetica', size:12), margin:[top:0, bottom:0])
        paragraph.children << new Text(value:'FOO1', font:new Font(family:'Helvetica', size:13))
        paragraph.children << new LineBreak()
        paragraph.children << new Text(value:'FOO2', font:new Font(family:'Helvetica', size:14))
        cell.children << paragraph

        cellElement = new CellElement(cell, 0)
    }

    def "getLines returns both elements when given enough space"() {
        given:
        float height = cellElement.paragraphElements[0].totalHeight

        when:
        cellElement.parseUntilHeight(height)
        def lines = cellElement.currentLines

        then:
        lines.size() == 2
    }

    def "getLines returns one element when not enough space to render both"() {
        given:
        float height = cellElement.paragraphElements[0].totalHeight - 1

        when:
        cellElement.parseUntilHeight(height)
        def lines = cellElement.currentLines

        then:
        lines.size() == 1
    }

}

