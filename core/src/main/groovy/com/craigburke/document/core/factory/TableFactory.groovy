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

class CellCounter {
	int totalCount = 0
	int _currentRowCount = 0
	
	def methodMissing(String name, args) {
		if (name == "row" && args?.last() instanceof Closure) {
			_currentRowCount = 0
			Closure rowClosure = args.last().clone()
			rowClosure.delegate = this
			rowClosure.resolveStrategy = Closure.DELEGATE_ONLY
			rowClosure()
		}
		if (name == "cell") {
			_currentRowCount++
			totalCount = Math.max(totalCount, _currentRowCount)
		}
		
	}

	def propertyMissing(String name) { [:] }
}