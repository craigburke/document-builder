package com.craigburke.document.core.factory

import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Text
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell

/**
 * Factory for cell nodes
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

		TextBlock paragraph = new TextBlock(font:cell.font.clone(), parent:cell, align:cell.align)
        builder.setNodeProperties(paragraph, [margin:[top:0, left:0, bottom:0, right:0]], 'paragraph')
		cell.children << paragraph

		if (value) {
			List elements = paragraph.addText(value.toString())
			elements.each { node ->
				if (node instanceof Text) {
					builder.setNodeProperties(node, [:], 'text')
				}
			}
		}

		cell
	}

	void onNodeCompleted(FactoryBuilderSupport builder, row, cell) {
		if (builder.onTextBlockComplete && cell.children) {
			builder.onTextBlockComplete(cell.children[0])
		}
   	}

}
