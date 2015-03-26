package com.craigburke.document.core.factory

import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Text

/**
 * Factory for text nodes
 * @author Craig Burke
 */
class TextFactory extends AbstractFactory {

	boolean isLeaf() { true }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        TextBlock paragraph = (builder.parentName == 'paragraph') ? builder.current : builder.current.children[0]
        List elements = paragraph.addText(value, builder.font.clone())
        List<Text> textElements = elements.findAll { it instanceof Text }

        textElements.each { Text text ->
            text.parent = paragraph
            builder.setStyles(text, attributes)
            text.font << attributes.font
            if (builder.addTextToTextBlock) {
                builder.addTextToTextBlock(text, paragraph)
            }
        }

		elements
	}

}
