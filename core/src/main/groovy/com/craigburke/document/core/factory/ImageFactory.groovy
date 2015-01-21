package com.craigburke.document.core.factory

import com.craigburke.document.core.Image
import com.craigburke.document.core.Paragraph

class ImageFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Image image = new Image(attributes)	

		switch (builder.parentName) {
			case "cell":
				builder.addImageToParagraph(image, builder.current.children[0])
				break
			case "paragraph":
				builder.addImageToParagraph(image, builder.current)
				break
		}

		image
	} 

	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
	}
	
}