package com.craigburke.document.core.factory

import com.craigburke.document.core.Column
import com.craigburke.document.core.Document
import com.craigburke.document.core.Table
import com.craigburke.document.core.builder.RenderState

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
		if (table.parent instanceof Column) {
			table.parent.children << table
		}
		builder.setNodeProperties(table, attributes, 'table')
        table
	}

	void setChild(FactoryBuilderSupport builder, table, row) {
		table.children << row
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, table) {
		if (parent instanceof Document || builder.renderState != RenderState.PAGE) {
			table.normalizeColumnWidths()
		}
		table.updateRowspanColumns()
		if (parent instanceof Document && builder.onTableComplete) {
			builder.onTableComplete(table)
		}
   	}
}
