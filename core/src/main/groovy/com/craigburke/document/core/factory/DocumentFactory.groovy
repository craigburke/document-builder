package com.craigburke.document.core.factory

import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.Margin
import com.craigburke.document.core.UnitCategory

class DocumentFactory extends AbstractFactory {
	
	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Document document = new Document(attributes)
        document.font = document.font ?: new Font()
        document.margin.setDefaults(72, 72)
        builder.createDocument(document, builder.out)

        builder.document = document
        document
	}

	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
		parent.children << child
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
		builder.writeDocument(builder.document, builder.out)
   	}

}