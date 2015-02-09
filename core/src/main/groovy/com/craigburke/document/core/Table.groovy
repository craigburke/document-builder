package com.craigburke.document.core

class Table extends BaseNode {
	List<Row> rows = []

	Border border = new Border()
	Integer columns
	Integer width = (72 * 6)
}