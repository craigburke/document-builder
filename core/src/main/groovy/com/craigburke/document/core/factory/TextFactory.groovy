package com.craigburke.document.core.factory

import com.craigburke.document.core.Font
import com.craigburke.document.core.Text

class TextFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Text text = new Text(value: value)
		text.font = attributes.font ? new Font(attributes.font) : builder.current.font.clone()

		switch (builder.parentName) {
			case "paragraph":
				builder.addTextToParagraph(text, builder.current)
				break
			case "cell":
				builder.addTextToCell(text, builder.current)
				break
		}

		text
	}

	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
		parent.children << child
	}
	
}