package com.craigburke.document.core.factory

import com.craigburke.document.core.PageBreak

/**
 * Factory for line break nodes
 * @author Craig Burke
 */
class PageBreakFactory extends AbstractFactory {

    boolean isLeaf() { true }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        PageBreak pageBreak = new PageBreak()
        pageBreak.parent = builder.document

        if (builder.addPageBreakToDocument) {
            builder.addPageBreakToDocument(pageBreak, builder.document)
        }

        pageBreak
    }

}
