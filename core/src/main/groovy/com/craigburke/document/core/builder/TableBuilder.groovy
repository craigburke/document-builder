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

	def addTableToDocument
	def addRowToTable
	
	def addCellToRow
	def addParagraphToCell
	
	def onTableComplete
	def onRowComplete
    def onCellComplete
}