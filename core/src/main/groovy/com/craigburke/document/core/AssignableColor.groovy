package com.craigburke.document.core

trait AssignableColor {
    Color color = new Color()

    void setColor(String value) {
        color.color = value
    }
}
