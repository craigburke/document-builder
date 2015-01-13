package com.craigburke.document.core.factory

class CreateFactory extends AbstractFactory {

    boolean isLeaf() { false }
    boolean onHandleNodeAttributes(builder, node, attributes) { false }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        []
    }

}