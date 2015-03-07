package com.craigburke.document.core

/**
 * Table node which contains children of children
 * @author Craig Burke
 */
class Table extends BaseNode implements BlockNode {
    final static int DEFAULT_HORIZONTAL_MARGIN = 0
    final static int DEFAULT_VERTICAL_MARGIN = 12

    List<Row> children = []

    Integer padding = 10
	Integer columns
	Integer width

    void updateColumnWidths() {
        Document document = parent

        int defaultPageWidth = document.width - document.margin.left - document.margin.right
        int defaultTableWidth = defaultPageWidth - margin.right - margin.left
        this.width = this.width ?: defaultTableWidth
        int totalBorderWidth = (columns + 1) * border.size

        def columnWidths = []
        int totalColumnWidth = 0
        def cells = this.children.first()?.children

        cells?.each {
            columnWidths << it.width
            totalColumnWidth += it.width ?: 0
        }

        int unspecifiedColumnCount = columnWidths.count { !it }

        if (unspecifiedColumnCount) {
            int remainingWidth = width - totalColumnWidth - totalBorderWidth
            int calculatedColumnWidth = Math.round(remainingWidth / unspecifiedColumnCount)
            columnWidths = columnWidths.collect { it != null ? it : calculatedColumnWidth }

            children.each { row ->
                row.children.eachWithIndex { cell, index ->
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
