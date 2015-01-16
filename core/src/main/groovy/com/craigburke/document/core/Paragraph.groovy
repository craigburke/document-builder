package com.craigburke.document.core

class Paragraph extends BaseNode {
	Margin margin = new Margin()
	Align align

	List children = []

	String getText() {
		String text = ""
		children.each { text += it.value }
		text
	}
}