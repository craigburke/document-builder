package com.craigburke.document.core.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.Image
import com.craigburke.document.core.Table
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text

trait TableBuilder {
	def tablePosition = [cell: 0, row: 0]

	abstract void addTableToDocument(Table table, Document document)
	abstract void addRowToTable(Row row, Table table)
	
	abstract void addCellToRow(Cell cell, Row row)
	abstract void addTextToCell(Text text, Cell cell)
	abstract void addImageToCell(Image image, Cell cell)

	def onTableComplete
	def onRowComplete
	def onCellComplete
}