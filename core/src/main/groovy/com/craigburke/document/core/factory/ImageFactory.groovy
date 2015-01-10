package com.craigburke.document.core.factory

import com.craigburke.document.core.Image
import com.craigburke.document.core.Paragraph

class ImageFactory extends AbstractFactory {
	
	boolean isLeaf() { true } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Image image = new Image(attributes)	
		def parent = builder.current

		Paragraph paragraph

		switch (builder.parentName) {
			case "cell":
				paragraph = builder.current.paragraphs[0]
				break
			case "paragraph":
				paragraph = builder.current
				break
		}
		builder.addImageToParagraph(image, parent)
		image
	} 

	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
	}
	
}