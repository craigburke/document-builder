package com.craigburke.document.core.factory

class LineBreakFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		def paragraph
		
		if (builder.parentName == "paragraph") {
			paragraph = builder.current
		}
		else if (builder.parentName == "cell") {
			paragraph = builder.current.paragraphs.last()
		}
		
		builder.addLineBreakToParagraph(paragraph)
		
		[:]
	}

}