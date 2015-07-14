package com.craigburke.document.core.factory

import com.craigburke.document.core.Row

/**
 * Factory for row nodes
 * @author Craig Burke
 */
class RowFactory extends AbstractFactory {

    boolean isLeaf() { false }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Row row = new Row(attributes)
        row.parent = builder.current
        builder.setNodeProperties(row, attributes, 'row')
        row
    }

    void setChild(FactoryBuilderSupport builder, row, column) {
        column.parent = row
        row.children << column
    }

}
