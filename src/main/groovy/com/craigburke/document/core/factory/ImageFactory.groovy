package com.craigburke.document.core.factory

import com.craigburke.document.core.Image

class ImageFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Image image = new Image(attributes)	
		def parent = builder.current
		
		switch (builder.parentName) {
			case "cell":
				builder.addImageToCell(image, parent)
				break
			case "paragraph":
				builder.addImageToParagraph(image, parent)
				break
		}
		image
	} 
	
	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
	}
	
}