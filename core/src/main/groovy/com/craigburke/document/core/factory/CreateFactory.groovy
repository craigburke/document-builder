package com.craigburke.document.core.factory

/**
 * Factory that creates a node and serves as wrapper for the document node
 * @author Craig Burke
 */
class CreateFactory extends AbstractFactory {

    boolean isLeaf() { false }
    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        [:]
    }

    void setChild(FactoryBuilderSupport builder, parent, child) {
        parent.document = child
    }

}
