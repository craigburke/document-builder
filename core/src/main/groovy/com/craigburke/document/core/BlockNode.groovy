package com.craigburke.document.core

/**
 * The base node for all block nodes
 * @author Craig Burke
 */
trait BlockNode {
    abstract final static Margin DEFAULT_MARGIN
    Align align = Align.LEFT
    Margin margin = new Margin()
    Border border = new Border()
}
