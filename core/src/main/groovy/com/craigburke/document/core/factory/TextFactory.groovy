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
        TextBlock paragraph
        if (builder.parentName == 'paragraph') {
            paragraph = builder.current
        }
        else {
            paragraph = builder.getColumnParagraph(builder.current)
        }
        List elements = paragraph.addText(value.toString())
         elements.each { node ->
            node.parent = paragraph
            if (node instanceof Text) {
                node.url = attributes.url
                builder.setNodeProperties(node, attributes, 'text')
            }
        }
		elements
	}

}
