package com.craigburke.document.core

class Margin {
    
    Integer top
    Integer bottom
    Integer left
    Integer right

    void setDefaults(int vertical, int horizontal) {
        top = (top == null) ? horizontal : top
        bottom = (bottom == null) ? horizontal : bottom
        left = (left == null) ? vertical : left
        right = (right == null) ? vertical : right
    }




}
