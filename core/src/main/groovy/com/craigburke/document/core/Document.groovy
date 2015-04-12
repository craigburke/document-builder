package com.craigburke.document.core

import static com.craigburke.document.core.UnitUtil.inchToPoint

/**
 * Document node
 * @author Craig Burke
 */
class Document extends BlockNode {
    static Margin defaultMargin = new Margin(top:72, bottom:72, left:72, right:72)

    int pageCount
    final int width = inchToPoint(8.5)
    final int height = inchToPoint(11)

    Map template
    def header
    def footer

    List children = []
    List<EmbeddedFont> embeddedFonts = []

}
