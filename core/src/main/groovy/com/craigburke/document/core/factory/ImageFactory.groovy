package com.craigburke.document.core.factory

import com.craigburke.document.core.Image
import com.craigburke.document.core.ImageType
import com.craigburke.document.core.TextBlock

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.security.MessageDigest

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

        if (!image.name || builder.imageFileNames.contains(image.name)) {
            image.name = generateImageName(image)
        }
        builder.imageFileNames << image.name

        TextBlock paragraph = builder.parentName == 'paragraph' ? builder.current : builder.current.children[0]
        image.parent = paragraph
        paragraph.children << image

        image
	}

    String generateImageName(Image image) {
        Formatter hexHash = new Formatter()
        MessageDigest.getInstance('SHA-1').digest(image.data).each {
            b -> hexHash.format('%02x', b)
        }
        "${hexHash}.${image.type == ImageType.JPG ? 'jpg' : 'png'}"
    }

}
