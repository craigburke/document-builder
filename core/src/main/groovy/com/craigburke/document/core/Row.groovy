package com.craigburke.document.core

/**
 * Table row node
 * @author Craig Burke
 */
class Row extends BaseNode implements Stylable, BackgroundAssignable {
	List<Column> children = []
    Integer width
}
