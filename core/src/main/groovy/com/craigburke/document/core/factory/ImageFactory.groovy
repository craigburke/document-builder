package com.craigburke.document.core.factory

import com.craigburke.document.core.Image
import com.craigburke.document.core.Paragraph

/**
 * Factory for image nodes
 * @author Craig Burke
 */
class ImageFactory extends AbstractFactory {

	boolean isLeaf() { true }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Image image = new Image(attributes)

        if (builder.parentName in ['paragraph', 'cell'] && builder.addImageToParagraph) {
            Paragraph paragraph = builder.parentName == 'paragraph' ? builder.current : builder.current.children[0]
            builder.addImageToParagraph(image, paragraph)
        }

		image
	}

	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
	}

}
