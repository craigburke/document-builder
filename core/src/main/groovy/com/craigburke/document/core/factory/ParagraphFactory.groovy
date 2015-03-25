package com.craigburke.document.core.factory

import com.craigburke.document.core.Align
import com.craigburke.document.core.Document
import com.craigburke.document.core.Text
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.builder.RenderState

/**
 * Factory for paragraph nodes
 * @author Craig Burke
 */
class ParagraphFactory extends AbstractFactory {

	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		Paragraph paragraph = new Paragraph(attributes)
		paragraph.parent = builder.parentName == 'create' ? builder.document : builder.current
		builder.setDefaults(paragraph)

        paragraph.font << attributes.font

        if (paragraph.parent instanceof Document) {
			paragraph.align = paragraph.align ?: Align.LEFT

			if (builder.renderState == RenderState.PAGE) {
				if (builder.addParagraphToDocument) {
					builder.addParagraphToDocument(paragraph, builder.current)
				}
			}
		}

		if (value) {
			List elements = paragraph.addText(value, paragraph.font)

			if (builder.addTextToParagraph) {
				elements.findAll { it instanceof Text }.each { Text text ->
					builder.addTextToParagraph(text, paragraph)
				}
            }
		}

		paragraph
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, child) {
		if (builder.onParagraphComplete) {
			builder.onParagraphComplete(child)
		}
   	}

}
