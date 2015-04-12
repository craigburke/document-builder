package com.craigburke.document.core

/**
 * An individual cell for the Table node
 * @author Craig Burke
 */
class Cell extends BaseNode implements Stylable, Alignable, BackgroundAssignable {
	List children = []
	Integer width
}
