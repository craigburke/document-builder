package com.craigburke.document.core.factory

import com.craigburke.document.core.Margin
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell

/**
 * Factory for cell nodes
 * @author Craig Burke
 */
class CellFactory extends AbstractFactory {

	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Cell cell = new Cell(attributes)
		Row row = builder.current
		builder.setDefaults(cell)

        cell.font << attributes.font

		cell.position = builder.tablePosition.cell
        if (builder.addCellToRow) {
            builder.addCellToRow(cell, row)
        }

		Paragraph paragraph = new Paragraph(font:cell.font.clone(), parent:cell, align:cell.align)
		paragraph.margin.setDefaults(new Margin(top:0, bottom:0, left:0, right:0))
        if (builder.addParagraphToCell) {
            builder.addParagraphToCell(paragraph, cell)
        }
		cell.children << paragraph

		if (value) {
			Text text = new Text(value:value, font:cell.font.clone(), parent:paragraph)
		    paragraph.children << text
            if (builder.addTextToParagraph) {
                builder.addTextToParagraph(text, paragraph)
            }
		}

		cell
	}

	void onNodeCompleted(FactoryBuilderSupport builder, row, cell) {
		if (builder.onParagraphComplete && cell.children) {
			builder.onParagraphComplete(cell.children[0])
		}
		if (builder.onCellComplete) {
			builder.onCellComplete(cell, row)
		}
   	}

}
