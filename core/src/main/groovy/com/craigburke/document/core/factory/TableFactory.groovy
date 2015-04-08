package com.craigburke.document.core.factory

import com.craigburke.document.core.Table

/**
 * Factory for table nodes
 * @author Craig Burke
 */
class TableFactory extends AbstractFactory {

	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Table table = new Table(attributes)
		table.parent = builder.parentName == 'create' ? builder.document : builder.current
		builder.setNodeProperties(table, attributes, 'table')
        table
	}

	void setChild(FactoryBuilderSupport builder, table, row) {
		table.children << row
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, table) {
        table.updateColumnWidths()

		if (builder.onTableComplete) {
			builder.onTableComplete(table)
		}
   	}

}
