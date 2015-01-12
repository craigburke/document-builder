package com.craigburke.document.core

class Table extends BaseNode {
	List<Row> rows = []

	Integer columns = 1
	BigDecimal width = (72 * 6)
	BigDecimal borderSize = 1
}