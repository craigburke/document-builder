package com.craigburke.document.core.factory

import com.craigburke.document.core.Table

class TableFactory extends AbstractFactory {
	
	boolean isLeaf() { false }
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	boolean isHandlesNodeChildren() { true }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		builder.tablePosition.row = 0
		
		Table table = new Table(attributes)
		table.font = table.font ?: builder.current.font.clone()		
		
		table
	}

	boolean onNodeChildren( FactoryBuilderSupport builder, table, Closure childContent) {
		if (!table.columns) {
			Closure childClone = childContent.clone()

			// determine the number of columns
			def cellCounter = new CellCounter()

			childClone.delegate = cellCounter
			childClone.resolveStrategy = Closure.DELEGATE_ONLY
			childClone()

			table.columns = cellCounter.totalCount
		}

		builder.addTableToDocument(table, builder.current)

		return true
	}


	void setChild(FactoryBuilderSupport builder, table, row) {
		row.parent = table
		table.rows << row
		builder.tablePosition.row++
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, table) {
		if (builder.onTableComplete instanceof Closure) {
			builder.onTableComplete(table)
		}
   	}
	
}

