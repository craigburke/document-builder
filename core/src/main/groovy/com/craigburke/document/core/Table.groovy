package com.craigburke.document.core

class Table extends BaseNode {
	List<Row> rows = []

	Border border = new Border()
    Margin margin = new Margin()
    Integer padding = 10
	Integer columns
	Integer width

    void updateColumnWidths() {
        Document document = parent

        int defaultTableWidth = document.width - document.margin.left - document.margin.right - margin.right - margin.left
        this.width = this.width ?: defaultTableWidth
        int totalBorderWidth = (columns + 1) * border.size

        def columnWidths = []
        int totalColumnWidth = 0
        def cells = this.rows.first()?.cells

        cells?.each {
            columnWidths << it.width
            totalColumnWidth += it.width ?: 0
        }

        int unspecifiedColumnCount = columnWidths.count { !it }

        if (unspecifiedColumnCount) {
            int remainingWidth = width - totalColumnWidth - totalBorderWidth
            int calculatedColumnWidth = Math.round(remainingWidth / unspecifiedColumnCount)
            columnWidths = columnWidths.collect { it != null ? it : calculatedColumnWidth}

            rows.each { row ->
                row.cells.eachWithIndex { cell, index ->
                    cell.width = columnWidths[index]
                }
            }
        }

        int totalDeclaredWidth = columnWidths.sum() + totalBorderWidth

        if (totalDeclaredWidth != width) {
            int diff = (width - totalDeclaredWidth)
            cells?.last().width += diff
        }

    }

 }