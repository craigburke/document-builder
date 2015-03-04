package com.craigburke.document.core

import static com.craigburke.document.core.UnitUtil.inchToPoint

/**
 * Document node
 * @author Craig Burke
 */
class Document extends BaseNode {
    final int width = inchToPoint(8.5)
    final int height = inchToPoint(11)

    def header

    List children = []
    List<EmbeddedFont> embeddedFonts = []
    Margin margin = new Margin()
}
