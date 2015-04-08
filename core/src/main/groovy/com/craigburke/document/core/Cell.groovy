package com.craigburke.document.core

/**
 * An individual cell for the Table node
 * @author Craig Burke
 */
class Cell extends BaseNode implements StyledNode, AlignedNode {
	List children = []
	Integer width
}
