package com.craigburke.document.core.factory

class LineBreakFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		
		if (builder.parentName == "paragraph") {
			builder.addLineBreakToParagraph(builder.current)
		}
		else if (builder.parentName == "cell") {
			builder.addLineBreakToParagraph(builder.current.children[0])
		}

		[:]
	}

}