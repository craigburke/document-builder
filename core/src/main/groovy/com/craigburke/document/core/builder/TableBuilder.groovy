package com.craigburke.document.core.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.Table
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import com.craigburke.document.core.Image

trait TableBuilder {
	int currentCellPosition
	int currentRowPosition
	
	abstract void addTableToDocument(Table table, Document document)
	abstract void addRowToTable(Row row, Table table)
	
	abstract void addCellToRow(Cell cell, Row row)
	abstract void addParagraphToCell(Paragraph paragraph, Cell cell)

	def onTableComplete
	def onRowComplete
	def onCellComplete
}