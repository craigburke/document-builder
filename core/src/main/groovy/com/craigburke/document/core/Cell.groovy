package com.craigburke.document.core

class Cell extends BaseNode {
	Font font
	List<Paragraph> paragraphs = []

	Integer position = 0
	BigDecimal width
}