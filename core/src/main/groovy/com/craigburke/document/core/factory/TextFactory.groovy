package com.craigburke.document.core.factory

import com.craigburke.document.core.Font
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text

class TextFactory extends AbstractFactory {
	
	boolean isLeaf() { true }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Text text = new Text(value: value)
		text.font = attributes.font ? new Font(attributes.font) : builder.current.font.clone()

        Paragraph paragraph = (builder.parentName == "paragraph") ? builder.current : builder.current.children[0]
        if (builder.addTextToParagraph) {
            builder.addTextToParagraph(text, paragraph)
        }
        paragraph.children << text

		text
	}

}