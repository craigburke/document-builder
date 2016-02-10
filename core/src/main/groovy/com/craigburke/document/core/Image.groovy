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
    private URL imageUrl
    private byte[] imageData

    void setType(String value) {
        type = Enum.valueOf(ImageType, value.toUpperCase())
    }

    URL getUrl() {
        imageUrl
    }

    void setUrl(String value) {
        setUrl(value != null ? URI.create(value).toURL() : null)
    }

    void setUrl(URL value) {
        imageUrl = value
    }

    byte[] getData() {
        imageUrl?.bytes ?: imageData
    }

    void setData(byte[] data) {
        imageData = Arrays.copyOf(data, data.length)
    }

    def withInputStream(Closure work) {
        if(imageUrl != null) {
            return imageUrl.withInputStream(work)
        }
        work.call(new ByteArrayInputStream(imageData))
    }

    String getHash() {
        Formatter hexHash = new Formatter()
        MessageDigest.getInstance('SHA-1').digest(getData()).each {
            b -> hexHash.format('%02x', b)
        }
        hexHash.toString()
    }
}
