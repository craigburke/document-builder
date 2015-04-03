package com.craigburke.document.core.factory

import com.craigburke.document.core.Heading
import com.craigburke.document.core.Text

/**
 * Created by craig on 3/25/15.
 */
class HeadingFactory extends AbstractFactory {

    boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Heading heading = new Heading(attributes)
        heading.level = Integer.valueOf(builder.currentName - 'heading')
        heading.parent = builder.document
        builder.setStyles(heading, attributes, 'heading')
        Text text = new Text(value:value, parent:heading)
        heading.children << text
        builder.setStyles(text, [:], 'text')

        if (builder.addTextToTextBlock) {
            builder.addTextToTextBlock(text, heading)
        }

        heading
    }

    void onNodeCompleted(FactoryBuilderSupport builder, parent, child) {
        if (builder.onTextBlockComplete) {
            builder.onTextBlockComplete(child)
        }
    }

}
