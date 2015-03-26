package com.craigburke.document.core.factory

import com.craigburke.document.core.Align
import com.craigburke.document.core.Document
import com.craigburke.document.core.Text
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.builder.RenderState

/**
 * Factory for paragraph nodes
 * @author Craig Burke
 */
class ParagraphFactory extends AbstractFactory {

	boolean isLeaf() { false }
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
		TextBlock paragraph = new TextBlock(attributes)
		paragraph.parent = builder.parentName == 'create' ? builder.document : builder.current
		builder.setStyles(paragraph, attributes)

        if (paragraph.parent instanceof Document) {
			paragraph.align = paragraph.align ?: Align.LEFT

			if (builder.renderState == RenderState.PAGE) {
				if (builder.addTextBlockToDocument) {
					builder.addTextBlockToDocument(paragraph, builder.current)
				}
			}
		}

		if (value) {
			List elements = paragraph.addText(value.toString(), paragraph.font)

			if (builder.addTextToTextBlock) {
				elements.findAll { it instanceof Text }.each { Text text ->
					builder.setStyles(text, [:])
					builder.addTextToTextBlock(text, paragraph)
				}
            }
		}

		paragraph
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, child) {
		if (builder.onTextBlockComplete) {
			builder.onTextBlockComplete(child)
		}
   	}

}
