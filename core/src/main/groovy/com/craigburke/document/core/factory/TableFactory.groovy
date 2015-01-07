package com.craigburke.document.core.factory

import com.craigburke.document.core.Table

class TableFactory extends AbstractFactory {
	
	boolean isLeaf() { false }
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		builder.currentRowPosition = 0
		
		Table table = new Table(attributes)
		table.font = table.font ?: builder.current.font.clone()		
		builder.addTableToDocument(table, builder.current)		
		
		table
	}

	void setChild(FactoryBuilderSupport builder, table, row) {
		row.parent = table
		table.rows << row
		builder.currentRowPosition++
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, table) {
		if (builder.onTableComplete instanceof Closure) {
			builder.onTableComplete(table)
		}
   	}
	
}