package com.craigburke.document.core

/**
 * The base node for all block nodes
 * @author Craig Burke
 */
trait BlockNode implements AlignedNode {
    abstract final static Margin DEFAULT_MARGIN
    Margin margin = new Margin()
    Border border = new Border()
}
