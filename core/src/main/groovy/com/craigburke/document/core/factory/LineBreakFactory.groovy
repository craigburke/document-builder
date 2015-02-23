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

		if (builder.parentName in ['paragraph', 'cell'] && builder.addLineBreakToParagraph) {
		    Paragraph paragraph = builder.parentName == 'paragraph' ? builder.current : builder.current.children[0]
		    builder.addLineBreakToParagraph(lineBreak, paragraph)
        }

        lineBreak
	}

}
