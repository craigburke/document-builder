package com.craigburke.document.core

/**
 * Image node
 * @author Craig Burke
 */
class Image extends BaseNode {
    String name
    ImageType type = ImageType.JPG
    Integer width
    Integer height
    byte[] data
    
    void setType(String value) { 
        type = Enum.valueOf(ImageType, value.toUpperCase()) 
    }
}
