package com.craigburke.document.core.factory

import com.craigburke.document.core.BaseNode

class LineBreakFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		if (builder.parentName == "paragraph") {
			builder.addLineBreakToParagraph(builder.current)
		}
		
		new BaseNode()
	}

}