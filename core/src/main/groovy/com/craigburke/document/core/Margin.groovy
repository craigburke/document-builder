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

    void setDefaults(int vertical, int horizontal) {
        top = (top == null) ? vertical : top
        bottom = (bottom == null) ? vertical : bottom
        left = (left == null) ? horizontal : left
        right = (right == null) ? horizontal : right
    }
}
