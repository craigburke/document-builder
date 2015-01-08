package com.craigburke.document.core.factory

import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell

class CellFactory extends AbstractFactory {
	
	boolean isLeaf() { false } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Cell cell = new Cell(attributes)
		Row row = builder.current
		
		cell.font = cell.font ?: row.font.clone()
		cell.position = builder.tablePosition.cell
		
		builder.addCellToRow(cell, row)
		
		if (value) {
			def paragraph = new Paragraph(font: cell.font)
			def text = new Text(value: value, font: cell.font)
			
			paragraph.children << text
			
			builder.addParagraphToCell(paragraph, cell)
			builder.addTextToParagraph(text, paragraph)
			
			cell.paragraphs << paragraph
		}
		
		cell
	}
	
	void setChild(FactoryBuilderSupport builder, cell, paragraph) {
		paragraph.parent = cell
		
		if (paragraph instanceof Paragraph) {
			cell.paragraphs << paragraph
		}
	}
	
	void onNodeCompleted(FactoryBuilderSupport builder, row, cell) {
		if (builder.onCellComplete instanceof Closure) {
			builder.onCellComplete(cell, row)
		}
   	}

}