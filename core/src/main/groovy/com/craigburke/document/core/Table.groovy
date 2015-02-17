package com.craigburke.document.core

class Table extends BaseNode {
	List<Row> rows = []

	Border border = new Border()
    Integer padding = 10
	Integer columns
	Integer width

    void updateColumnWidths() {
        Document document = parent

        int defaultTableWidth = document.width - document.margin.left - document.margin.right
        this.width = this.width ?: defaultTableWidth

        def columnWidths = []
        int totalColumnWidth = 0
        def cells = this.rows.first()?.cells

        cells?.each {
            columnWidths << it.width
            totalColumnWidth += it.width ?: 0
        }

        int unspecifiedColumnCount = columnWidths.count { it == null }

        if (unspecifiedColumnCount) {
            int remainingWidth = this.width - totalColumnWidth
            int calculatedColumnWidth = Math.round(remainingWidth / unspecifiedColumnCount)
            columnWidths = columnWidths.collect { it != null ? it : calculatedColumnWidth}
            rows.each { row ->
                row.cells.eachWithIndex { cell, index ->
                    cell.width = columnWidths[index]
                }
            }
        }
    }

 }