package com.craigburke.document.core

trait BackgroundAssignable {
    Color backgroundColor
    
    void setBackgroundColor(String value) {
        if (value) {
            backgroundColor = backgroundColor ?: new Color()
            backgroundColor.color = value
        }
    }
}
