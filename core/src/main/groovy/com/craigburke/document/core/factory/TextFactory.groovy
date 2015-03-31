package com.craigburke.document.core.factory

import com.craigburke.document.core.LineBreak
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
        List elements = paragraph.addText(value.toString())
         elements.each { node ->
            node.parent = paragraph
            if (node instanceof Text) {
                builder.setStyles(node, attributes, 'text')
                if (builder.addTextToTextBlock) {
                    builder.addTextToTextBlock(node, paragraph)
                }
            }
            else if (node instanceof LineBreak) {
                if (builder.addLineBreakToTextBlock) {
                    builder.addLineBreakToTextBlock(node, paragraph)
                }
            }
        }

		elements
	}

}
