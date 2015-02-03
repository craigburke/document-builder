package com.craigburke.document.core.factory

import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell

class CellFactory extends AbstractFactory {
	
	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Cell cell = new Cell(attributes)
		Row row = builder.current
		
		cell.font = cell.font ?: row.font.clone()
		cell.position = builder.tablePosition.cell
		builder.addCellToRow(cell, row)

		Paragraph paragraph = new Paragraph()
		paragraph.margin.setDefaults(0, 0)
		builder.addParagraphToCell(paragraph, cell)
		cell.children << paragraph
		
		if (value) {
			Text text = new Text(value: value, font: cell.font, parent: cell)
			builder.addTextToParagraph(text, paragraph)
		}

		cell
	}

	void onNodeCompleted(FactoryBuilderSupport builder, row, cell) {
		if (builder.onCellComplete instanceof Closure) {
			builder.onCellComplete(cell, row)
		}
   	}

}