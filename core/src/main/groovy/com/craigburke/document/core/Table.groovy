package com.craigburke.document.core

class Table extends BaseNode {
	List<Row> rows = []

	Border border = new Border()
	Integer columns
	BigDecimal width = (72 * 6)
}