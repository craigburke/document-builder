package com.craigburke.document.core

/**
 * An individual cell for the Table node
 * @author Craig Burke
 */
class Cell extends BaseNode implements StyledNode {
	List children = []
	Align align = Align.LEFT

	Integer position = 0
	Integer width
}
