package com.craigburke.document.core

trait Alignable {
    Align align = Align.LEFT

    void setAlign(String value) {
        align = Enum.valueOf(Align, value.toUpperCase())
    }
}
