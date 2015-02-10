package com.craigburke.document.core

class Document extends BaseNode {
    final int width = 8.5 * 72
    final int height = 11 * 72

    List children = []
    Margin margin = new Margin()
}
