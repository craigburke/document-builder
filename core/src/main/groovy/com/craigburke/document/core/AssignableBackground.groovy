package com.craigburke.document.core

trait AssignableBackground {
    Color backgroundColor
    
    void setBackgroundColor(String value) {
        if (value) {
            backgroundColor = backgroundColor ?: new Color()
            backgroundColor.color = value
        }
    }
}
