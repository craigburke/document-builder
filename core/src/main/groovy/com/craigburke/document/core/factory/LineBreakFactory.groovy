package com.craigburke.document.core.factory

import com.craigburke.document.core.LineBreak

class LineBreakFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		LineBreak lineBreak = new LineBreak()

		if (builder.parentName == "paragraph") {
			builder.addLineBreakToParagraph(lineBreak, builder.current)
		}
		else if (builder.parentName == "cell") {
			builder.addLineBreakToParagraph(lineBreak, builder.current.children[0])
		}

        lineBreak
	}

}