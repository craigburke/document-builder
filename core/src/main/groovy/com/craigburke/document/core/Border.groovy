package com.craigburke.document.core

/**
 * border settings for document block nodes
 * @author Craig Burke
 */
class Border implements ColorAssignable {
    Integer size = 1

    def leftShift(Map properties) {
        properties?.each { key, value -> this[key] = value }
    }
}
