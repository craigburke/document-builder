package com.craigburke.document.core

import java.security.MessageDigest

/**
 * Image node
 * @author Craig Burke
 */
class Image extends BaseNode {
    String name
    ImageType type = ImageType.JPG
    Integer width
    Integer height
    String url
    byte[] data

    void setType(String value) {
        type = Enum.valueOf(ImageType, value.toUpperCase())
    }

    byte[] getData() {
        if(this.@data == null && url != null) {
            this.data = new URL(url).bytes
        }
        this.@data
    }

    def withInputStream(Closure work) {
        work.call(new ByteArrayInputStream(getData()))
    }

    String getHash() {
        Formatter hexHash = new Formatter()
        MessageDigest.getInstance('SHA-1').digest(getData()).each {
            b -> hexHash.format('%02x', b)
        }
        hexHash.toString()
    }
}
