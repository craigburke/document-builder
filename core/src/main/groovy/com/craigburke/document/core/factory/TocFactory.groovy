package com.craigburke.document.core.factory

import com.craigburke.document.core.Toc

/**
 * Table of Contents factory.
 */
class TocFactory extends AbstractFactory {

    boolean isLeaf() { true }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Toc toc = new Toc(attributes)
        toc.parent = builder.parentName == 'create' ? builder.document : builder.current
        builder.setNodeProperties(toc, attributes, 'toc')
        toc
    }
}
