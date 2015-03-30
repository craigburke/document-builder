package com.craigburke.document.core

trait AlignedNode {
    Align align = Align.LEFT

    void setAlign(String value) {
        align = Enum.valueOf(Align, value.toUpperCase())
    }
}
