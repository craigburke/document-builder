package com.craigburke.document.core

class Cell extends BaseNode {
	List<Paragraph> paragraphs = []
	Integer padding = 2
	Align align = Align.LEFT

	Integer position = 0
	BigDecimal width
}