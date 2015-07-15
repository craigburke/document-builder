package com.craigburke.document.core

/**
 * Table node which contains children of children
 * @author Craig Burke
 */
class Table extends BlockNode implements BackgroundAssignable {
    static Margin defaultMargin = new Margin(top: 12, bottom: 12, left: 0, right: 0)
    List<Row> children = []

    Integer padding = 10
    Integer width
    List<BigDecimal> columns = []

    int getColumnCount() {
        if (columns) {
            columns.size()
        } else {
            (children) ? children.max { it.children.size() }.children.size() : 0
        }
    }

    void normalizeColumnWidths() {
        updateRowspanColumns()

        width = Math.min(width ?: maxWidth, maxWidth)
        if (!columns) {
            columnCount.times { columns << 1 }
        }
        BigDecimal relativeTotal = columns.sum()
        BigDecimal totalBorderWidth = (columnCount + 1) * border.size
        BigDecimal totalCellWidth = width - totalBorderWidth

        List<BigDecimal> columnWidths = []
        columns.eachWithIndex { column, index ->
            if (index == columns.size() - 1) {
                columnWidths << totalCellWidth - ((columnWidths.sum() ?: 0) as BigDecimal)
            } else {
                columnWidths << (Math.ceil((columns[index] / relativeTotal) * totalCellWidth) as BigDecimal)
            }
        }

        children.each { row ->
            int columnWidthIndex = 0
            row.children.eachWithIndex { column, index ->
                int endIndex = columnWidthIndex + column.colspan - 1
                BigDecimal missingBorderWidth = (column.colspan - 1) * border.size
                column.width = columnWidths[columnWidthIndex..endIndex].sum() + missingBorderWidth
                columnWidthIndex += column.colspan
                column.children.findAll { it instanceof Table }.each { it.normalizeColumnWidths() }
            }
        }
    }

    void updateRowspanColumns() {
        def updatedColumns = []

        children.eachWithIndex { row, rowIndex ->
            row.children.eachWithIndex { column, columnIndex ->
                if (column.rowspan > 1 && !updatedColumns.contains(column)) {
                    int rowspanEnd = Math.min(children.size() - 1, rowIndex + column.rowspan - 1)
                    (rowIndex + 1..rowspanEnd).each {
                        children[it].children.addAll(columnIndex, [column])
                    }
                    updatedColumns << column
                }
            }
        }
    }

    private int getMaxWidth() {
        if (parent instanceof Document) {
            parent.width - parent.margin.left - parent.margin.right
        } else if (parent instanceof Cell) {
            Table outerTable = parent.parent.parent
            parent.width - (outerTable.padding * 2)
        } else {
            0
        }

    }

}
