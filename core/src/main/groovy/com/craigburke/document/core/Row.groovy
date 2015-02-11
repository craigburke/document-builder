package com.craigburke.document.core

class Row extends BaseNode {
	List<Cell> cells = []
	Integer position = 0
    Integer width

    int getMaxCellPadding() {
        cells.max { it.padding }.padding
    }
}