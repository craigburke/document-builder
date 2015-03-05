package com.craigburke.document.core.factory

import com.craigburke.document.core.Align
import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
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
		paragraph.parent = paragraph.parent ?: builder.document

        paragraph.font = builder.font ? builder.font.clone() : new Font()
        paragraph.font << attributes.font

        if (paragraph.parent instanceof Document) {
			paragraph.align = paragraph.align ?: Align.LEFT

			switch (builder.renderState) {
				case RenderState.PAGE:
					paragraph.margin.setDefaults(8, 0)
					if (builder.addParagraphToDocument) {
						builder.addParagraphToDocument(paragraph, builder.current)
					}
					break

				case RenderState.HEADER:
					paragraph.margin.setDefaults(18, builder.document.margin.left)
					break

				case RenderState.FOOTER:
					paragraph.margin.setDefaults(0, builder.document.margin.left)
					break

			}
		}

		if (value) {
			Text text = new Text(value:value, font:paragraph.font.clone(), parent:paragraph)
			if (builder.addTextToParagraph) {
                builder.addTextToParagraph(text, paragraph)
            }
			paragraph.children << text
		}

		paragraph
	}

 	void onNodeCompleted(FactoryBuilderSupport builder, parent, child) {
		if (builder.onParagraphComplete) {
			builder.onParagraphComplete(child)
		}
   	}

}
