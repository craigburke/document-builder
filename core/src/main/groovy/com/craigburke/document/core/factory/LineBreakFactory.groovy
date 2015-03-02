package com.craigburke.document.core.factory

import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.Paragraph

/**
 * Factory for line break nodes
 * @author Craig Burke
 */
class LineBreakFactory extends AbstractFactory {

	boolean isLeaf() { true }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		LineBreak lineBreak = new LineBreak()

		Paragraph paragraph = builder.parentName == 'paragraph' ? builder.current : builder.current.children[0]
		lineBreak.parent = paragraph
		
		if (builder.parentName in ['paragraph', 'cell'] && builder.addLineBreakToParagraph) {
		    builder.addLineBreakToParagraph(lineBreak, paragraph)
        }
		paragraph.children << lineBreak

		lineBreak
	}

}
