package com.craigburke.document.core.factory

import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text

class TextFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Text text = new Text(value: value, font: attributes.font)
		text.font = text.font ?: builder.current.font

		Paragraph paragraph

		switch (builder.parentName) {
			case "paragraph":
				paragraph = builder.current
				break
			case "cell":
				paragraph = builder.current.paragraphs.last()
				break
		}

		builder.addTextToParagraph(text, paragraph)
		text
	}

	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
		parent.children << child
	}
	
}