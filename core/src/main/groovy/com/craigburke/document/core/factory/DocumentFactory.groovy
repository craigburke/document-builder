package com.craigburke.document.core.factory

import com.craigburke.document.core.Document

/**
 * Factory for document nodes
 * @author Craig Burke
 */
class DocumentFactory extends AbstractFactory {

	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Document document = new Document(attributes)
        builder.document = document
        builder.setNodeProperties(document, attributes, 'document')
        builder.initializeDocument(document, builder.out)

        document
	}

	void setChild(FactoryBuilderSupport builder, parent, child) {
		parent.children << child
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
		builder.writeDocument(builder.document, builder.out)
   	}

}
