package com.craigburke.document.core.factory

import com.craigburke.document.core.Align
import com.craigburke.document.core.Text
import com.craigburke.document.core.Paragraph

class ParagraphFactory extends AbstractFactory {
	
	boolean isLeaf() { false }
	boolean onHandleNodeAttributes(builder, node, attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Paragraph paragraph = new Paragraph(attributes)
		paragraph.font = paragraph.font ?: builder.current.font.clone()
		paragraph.margin.setDefaults(12, 0)
		
		switch (builder.parentName) {
			case "document":
				paragraph.align = paragraph.align ?: Align.LEFT
				builder.addParagraphToDocument(paragraph, builder.current)
				break
			case "cell":
				paragraph.align = paragraph.align ?: builder.current.align
				builder.addParagraphToCell(paragraph, builder.current)
				break
		}
		
		if (value) {
			Text text = new Text(value: value, font: paragraph.font, parent: paragraph)
			builder.addTextToParagraph(text, paragraph)
			paragraph.children << text
		}
		
		paragraph
	}
	
	void setChild(FactoryBuilderSupport builder, parent, child) {
		child.parent = parent
		parent.children << child
	}
	
 	void onNodeCompleted(FactoryBuilderSupport builder, parent, current) {
		if (builder.onParagraphComplete instanceof Closure) {
			builder.onParagraphComplete(current)
		}
   	}
	
	
}