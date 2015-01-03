package com.craigburke.document.core.factory

import com.craigburke.document.core.Text

class TextFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Text text = new Text(value: value, font: attributes.font)
		text.font = text.font ?: builder.current.font
		
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
	
}