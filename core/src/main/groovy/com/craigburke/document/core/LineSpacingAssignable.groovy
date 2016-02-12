package com.craigburke.document.core

/**
 * A trait for nodes that can have line spacing assigned to them (paragraph, table cell, etc.).
 */
trait LineSpacingAssignable {

    LineSpacing lineSpacingAttributes = new LineSpacing()

    Integer getLineSpacing() {
        lineSpacingAttributes.lineSpacing
    }

    void setLineSpacing(Number arg) {
        lineSpacingAttributes.lineSpacing = arg
    }

    BigDecimal getLineSpacingMultiplier() {
        lineSpacingAttributes.lineSpacingMultiplier
    }

    void setLineSpacingMultiplier(Number arg) {
        lineSpacingAttributes.lineSpacingMultiplier = arg
    }
}
