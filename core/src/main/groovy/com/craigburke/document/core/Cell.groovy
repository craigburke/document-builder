package com.craigburke.document.core

class Cell extends BaseNode {
	List<Paragraph> paragraphs = []
	Integer padding = 2
	
	Integer position = 0
	BigDecimal width
}