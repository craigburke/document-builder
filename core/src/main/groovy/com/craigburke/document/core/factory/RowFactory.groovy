package com.craigburke.document.core.factory

import com.craigburke.document.core.Row

class RowFactory extends AbstractFactory {
	
	boolean isLeaf() { false } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		builder.currentCellPosition = 0		
		Row row = new Row(attributes)
		
		row.position = builder.currentRowPosition
		row.font = row.font ?: builder.current.font.clone()
	 	builder.addRowToTable(row, builder.current)
		
		row
	}
 	
	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
		parent.cells << child
		builder.currentCellPosition++
	}
	
	void onNodeCompleted(FactoryBuilderSupport builder, table, row) {
		if (builder.onRowComplete instanceof Closure) {
			builder.onRowComplete(row)
		}
   	}
	
}