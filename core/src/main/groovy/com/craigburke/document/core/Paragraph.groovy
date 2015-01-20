package com.craigburke.document.core

class Paragraph extends BaseNode {
	Margin margin = new Margin()
	Align align
	
	BigDecimal leading
	BigDecimal leadingMultiplier = 1.1
	
	List children = []

	BigDecimal getLeading() {
		if (leading) {
			leading
		}
		else {
			leadingMultiplier * children.inject(0f) { max, child -> Math.max(max, child.font?.size ?: 0 as Float) }
		}
	}
	
	String getText() {
		String text = ""
		children.each { text += it.value }
		text
	}
}