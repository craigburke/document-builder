package com.craigburke.document.core

trait ColorAssignable {
    Color color = new Color()

    void setColor(String value) {
        color.color = value
    }
}
