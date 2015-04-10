package com.craigburke.document.core

/**
 * Created by craig on 3/25/15.
 */
class Heading extends TextBlock implements LinkNode {
    static final FONT_SIZE_MULTIPLIERS = [2, 1.5, 1.17, 1.12, 0.83, 0.75]
    int level = 1
}
