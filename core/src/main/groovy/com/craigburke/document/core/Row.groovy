package com.craigburke.document.core

class Row {
	def item
	Font font
	List<Cell> children = []
	Table parent
	
	Integer position = 0
}