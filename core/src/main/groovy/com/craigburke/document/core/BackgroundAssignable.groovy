package com.craigburke.document.core

trait BackgroundAssignable {
    Color background
    
    void setBackground(String value) {
        if (value) {
            background = background ?: new Color()
            background.color = value
        }
    }
}
