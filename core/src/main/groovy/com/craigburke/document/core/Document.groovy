package com.craigburke.document.core

class Document {
	def item
	Font font = new Font()

	List children = []

	BigDecimal marginTop = 72
	BigDecimal marginRight = 72
	BigDecimal marginBottom = 72
	BigDecimal marginLeft = 72
}