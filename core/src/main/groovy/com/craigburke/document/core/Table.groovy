package com.craigburke.document.core

/**
 * Table node which contains children of children
 * @author Craig Burke
 */
class Table extends BlockNode implements BackgroundAssignable {
    static Margin defaultMargin = new Margin(top:12, bottom:12, left:0, right:0)
    List<Row> children = []

    Integer padding = 10
	Integer width

    int getColumns() {
        (children) ? children.max { it.children.size() }.children.size() : 0
    }

    void normalizeColumnWidths() {
        this.width = Math.min(this.width ?: maxWidth, maxWidth)
        int totalBorderWidth = (columns + 1) * border.size

        def columnWidths = []
        int totalColumnWidth = 0
        def columns = this.children.first()?.children

        columns?.each {
            columnWidths << it.width
            totalColumnWidth += it.width ?: 0
        }

        int unspecifiedColumnCount = columnWidths.count { !it }

        if (unspecifiedColumnCount) {
            int remainingWidth = width - totalColumnWidth - totalBorderWidth
            int calculatedColumnWidth = Math.round(remainingWidth / unspecifiedColumnCount)
            columnWidths = columnWidths.collect { it != null ? it : calculatedColumnWidth }

            children.each { row ->
                row.children.eachWithIndex { column, index ->
                    column.width = columnWidths[index]
                }
            }
        }

        int totalDeclaredWidth = columnWidths.sum() + totalBorderWidth

        if (totalDeclaredWidth != width) {
            int diff = (width - totalDeclaredWidth)
            columns?.last().width += diff
        }

        children.each { row ->
            row.children.each { column ->
                column.children.findAll { it instanceof Table }.each { it.normalizeColumnWidths() }
            }
        }
    }

    private int getMaxWidth() {
        if (parent instanceof Document) {
            parent.width - parent.margin.left - parent.margin.right
        }
        else if (parent instanceof Column) {
            Table outerTable = parent.parent.parent
            parent.width - (outerTable.padding * 2)
        }
        else {
            0
        }

    }

}
