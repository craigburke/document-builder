package com.craigburke.document.core.factory

import com.craigburke.document.core.Table
import com.craigburke.document.core.builder.RenderState

/**
 * Factory for table nodes
 * @author Craig Burke
 */
class TableFactory extends AbstractFactory {

	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }
	boolean isHandlesNodeChildren() { true }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		builder.tablePosition.row = 0

		Table table = new Table(attributes)
        table.margin.setDefaults(8, 0)
		table.parent = table.parent ?: builder.document

		table.font = builder.font ? builder.font.clone() : builder.document.font.clone()
        table.font << attributes.font
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

        if (builder.renderState == RenderState.PAGE && builder.addTableToDocument) {
            builder.addTableToDocument(table, builder.current)
        }

		true
	}

	void setChild(FactoryBuilderSupport builder, table, row) {
		row.parent = table
		table.children << row
		builder.tablePosition.row++
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, table) {
        table.updateColumnWidths()

		if (builder.onTableComplete) {
			builder.onTableComplete(table)
		}
   	}

}
