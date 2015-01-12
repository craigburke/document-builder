package com.craigburke.document.core

class Table extends BaseNode {
	Font font
	List<Row> rows = []

	Integer columns = 1
	BigDecimal width = (72 * 6)
	BigDecimal borderSize = 1
}