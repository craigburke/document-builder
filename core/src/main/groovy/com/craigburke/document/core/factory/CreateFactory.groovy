package com.craigburke.document.core.factory

/**
 * Factory that creates a node and serves as wrapper for the document node
 * @author Craig Burke
 */
class CreateFactory extends AbstractFactory {

    boolean isLeaf() { false }
    boolean onHandleNodeAttributes(builder, node, attributes) { false }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        []
    }

}
