package com.craigburke.document.core.factory

import com.craigburke.document.core.BaseNode
import com.craigburke.document.core.Link
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Text

/**
 * Factory for link nodes
 * @author Craig Burke
 */
class LinkFactory extends AbstractFactory {

    boolean isLeaf() { true }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        TextBlock paragraph

        if (builder.parentName == 'paragraph') {
            paragraph = builder.current
        } else {
            paragraph = builder.getColumnParagraph(builder.current)
        }

        String text = attributes.value ?: value
        String url = attributes.url ?: value

        List elements = paragraph.add(text, true)

        elements.findAll { it instanceof Link }.each { Link link ->
            link.url = url
            link.style = attributes.style
            builder.setNodeProperties(link, attributes, 'link')
        }

        elements
    }

}
