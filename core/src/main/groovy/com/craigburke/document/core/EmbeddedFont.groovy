package com.craigburke.document.core

/**
 * Font embedded within a document
 * @author Craig Burke
 */
class EmbeddedFont {
    File file
    InputStream inputStream
    String name
    boolean italic = false
    boolean bold = false
}
