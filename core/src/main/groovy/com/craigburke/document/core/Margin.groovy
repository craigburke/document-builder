package com.craigburke.document.core

import groovy.transform.AutoClone

/**
 * Margin settings used for block nodes
 * @author Craig Burke
 */
@AutoClone
class Margin {
    static final Margin NONE = new Margin(top:0, right:0, bottom:0, left:0)

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
