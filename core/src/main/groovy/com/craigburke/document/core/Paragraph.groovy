package com.craigburke.document.core

class Paragraph {
	def item
	def parent
	List<Text> children = []
	
	Font font
	
	String getText() {
		String text = ""
		children.each { text += it.value }
		text
	}
	
	BigDecimal marginTop = 12
	BigDecimal marginRight = 12
	BigDecimal marginBottom = 12
	BigDecimal marginLeft = 12
}