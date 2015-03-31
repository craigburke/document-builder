package com.craigburke.document.core

/**
 * Margin settings used for block nodes
 * @author Craig Burke
 */
class Margin {

    Integer top
    Integer bottom
    Integer left
    Integer right

    void setDefaults(Margin defaultMargin) {
        top = (top == null) ? defaultMargin.top : top
        bottom = (bottom == null) ? defaultMargin.bottom : bottom
        left = (left == null) ? defaultMargin.left : left
        right = (right == null) ? defaultMargin.right : right
    }

    def leftShift(Map properties) {
        properties?.each { key, value -> this[key] = value }
    }

}
