package com.craigburke.document.core.factory

import com.craigburke.document.core.Image
import com.craigburke.document.core.TextBlock

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Factory for image nodes
 * @author Craig Burke
 */
class ImageFactory extends AbstractFactory {

	boolean isLeaf() { true }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Image image = new Image(attributes)

        if (!image.width || !image.height) {
            InputStream inputStream = new ByteArrayInputStream(image.data)
            BufferedImage bufferedImage = ImageIO.read(inputStream)
            image.width = bufferedImage.width
            image.height = bufferedImage.height
        }

        TextBlock paragraph = builder.parentName == 'paragraph' ? builder.current : builder.current.children[0]
        image.parent = paragraph
        paragraph.children << image

        image
	}
}
