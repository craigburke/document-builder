package com.craigburke.document.core

class Cell {
	def item
	Font font
	Row parent
	List paragraphs = []

	Integer position = 0
	BigDecimal width
}