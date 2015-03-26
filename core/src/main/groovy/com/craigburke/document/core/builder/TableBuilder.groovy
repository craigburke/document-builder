package com.craigburke.document.core.builder

trait TableBuilder {
	def tablePosition = [cell: 0, row: 0]

	def addTableToDocument
	def addRowToTable
	
	def addCellToRow
	def addTextBlockToCell
	
	def onTableComplete
	def onRowComplete
    def onCellComplete
}