package com.craigburke.document.core.factory

import com.craigburke.document.core.Heading
import com.craigburke.document.core.Text

/**
 * Factory for heading nodes
 * @author Craig Burke
 */
class HeadingFactory extends AbstractFactory {

    boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Heading heading = new Heading(attributes)
        heading.level = Integer.valueOf(builder.currentName - 'heading')
        heading.parent = builder.document
        builder.setNodeProperties(heading, attributes, 'heading')
        Text text = new Text(value:value, parent:heading)
        heading.children << text
        builder.setNodeProperties(text, [:], 'text')

        heading
    }

    void onNodeCompleted(FactoryBuilderSupport builder, parent, child) {
        if (builder.onTextBlockComplete) {
            builder.onTextBlockComplete(child)
        }
    }

}
