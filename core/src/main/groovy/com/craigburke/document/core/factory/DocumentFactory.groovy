package com.craigburke.document.core.factory

import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.UnitCategory

class DocumentFactory extends AbstractFactory {
	
	boolean isLeaf() { false } 
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Document document = new Document(attributes)
		document.font = document.font ?: new Font()
		document.margin.setDefaults(72, 72)
		document = builder.createDocument(document, builder.out)
		
		builder.document = document
		document
	}
	

	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
		parent.children << child
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, document) {
		builder.write(builder.document, builder.out)
   	}

}