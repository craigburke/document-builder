package com.craigburke.document.core.factory

import com.craigburke.document.core.Row

/**
 * Factory for row nodes
 * @author Craig Burke
 */
class RowFactory extends AbstractFactory {

	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Row row = new Row(attributes)
		row.parent = builder.current
		builder.setNodeProperties(row, attributes, 'row')
		row
	}

	void setChild(FactoryBuilderSupport builder, row, cell) {
		cell.parent = row
		row.children << cell
	}

	void onNodeCompleted(FactoryBuilderSupport builder, parent, child) {
		if (builder.onRowComplete) {
			builder.onRowComplete(child, parent)
		}
   	}

}
