package com.craigburke.document.core

class Margin {
    
    BigDecimal top
    BigDecimal bottom
    BigDecimal left
    BigDecimal right
    
    void setDefaults(BigDecimal vertical, BigDecimal horizontal) {
        top = (top == null) ? vertical : top
        bottom = (bottom == null) ? vertical : bottom
        left = (left == null) ? horizontal : left
        right = (right == null) ? horizontal : right
    }
    
}
