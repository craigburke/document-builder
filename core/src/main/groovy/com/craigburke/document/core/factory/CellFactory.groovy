package com.craigburke.document.core.factory

import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Text
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell

/**
 * Factory for column nodes
 * @author Craig Burke
 */
class CellFactory extends AbstractFactory {

    boolean isLeaf() { false }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Cell cell = new Cell(attributes)
        Row row = builder.current
        cell.parent = row
        builder.setNodeProperties(cell, attributes, 'cell')

        if (value) {
            TextBlock paragraph = builder.getColumnParagraph(cell)
            List elements = paragraph.addText(value.toString())
            elements.each { node ->
                if (node instanceof Text) {
                    builder.setNodeProperties(node, [:], 'text')
                }
            }
        }

        cell
    }

}
