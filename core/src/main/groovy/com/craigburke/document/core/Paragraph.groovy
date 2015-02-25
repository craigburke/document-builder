package com.craigburke.document.core

/**
 * Block element that holds text and images
 * @author Craig Burke
 */
class Paragraph extends BaseNode {
	Margin margin = new Margin()
	Align align

	Integer leading
	Integer leadingMultiplier = 1.1

	List children = []

	Integer getLineHeight() {
        leading ?: leadingMultiplier * children.findAll { it.getClass() == Text }.inject(0f) { max, child -> Math.max(max, child.font.size as Float) }
	}

	String getText() {
		children.findAll { it.getClass() == Text }*.value.join('')
	}
}
