package com.craigburke.document.core

class Paragraph extends BaseNode {
	Margin margin = new Margin()
	Align align
	BigDecimal leading

	List children = []

	String getText() {
		String text = ""
		children.each { text += it.value }
		text
	}
}