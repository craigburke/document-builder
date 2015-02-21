package com.craigburke.document.core.factory

import com.craigburke.document.core.Align
import com.craigburke.document.core.Font
import com.craigburke.document.core.Margin
import com.craigburke.document.core.Text
import com.craigburke.document.core.Paragraph

class ParagraphFactory extends AbstractFactory {
	
	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }
	
	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Paragraph paragraph = new Paragraph(attributes)
        paragraph.margin.setDefaults(8, 0)
        paragraph.font = paragraph.font ?: builder.current.font.clone()

        switch (builder.parentName) {
			case "document":
				paragraph.align = paragraph.align ?: Align.LEFT
                if (builder.addParagraphToDocument) {
                    builder.addParagraphToDocument(paragraph, builder.current)
                }
				break
			case "cell":
				paragraph.align = paragraph.align ?: builder.current.align
                if (builder.addParagraphToCell) {
                    builder.addParagraphToCell(paragraph, builder.current)
                }
				break
		}
		
		if (value) {
			Text text = new Text(value: value, font: paragraph.font.clone(), parent: paragraph)
			if (builder.addTextToParagraph) {
                builder.addTextToParagraph(text, paragraph)
            }
			paragraph.children << text
		}
		
		paragraph
	}
	
	void setChild(FactoryBuilderSupport builder, parent, child) {
        child.parent = parent
        if (!parent.children.contains(child)) {
            parent.children << child
        }
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, child) {
		if (builder.onParagraphComplete) {
			builder.onParagraphComplete(child)
		}
   	}
	
	
}