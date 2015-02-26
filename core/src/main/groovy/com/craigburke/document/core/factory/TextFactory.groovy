package com.craigburke.document.core.factory

import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text

/**
 * Factory for text nodes
 * @author Craig Burke
 */
class TextFactory extends AbstractFactory {

	boolean isLeaf() { true }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Paragraph paragraph = (builder.parentName == 'paragraph') ? builder.current : builder.current.children[0]

        Text text = new Text(value:value, parent:paragraph)
        text.font = builder.font.clone()
        text.font << attributes.font

        if (builder.addTextToParagraph) {
            builder.addTextToParagraph(text, paragraph)
        }
        paragraph.children << text

		text
	}

}
