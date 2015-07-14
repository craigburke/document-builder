package com.craigburke.document.builder

import com.craigburke.document.builder.render.ParagraphRenderer
import com.craigburke.document.core.BaseNode
import com.craigburke.document.core.Document
import com.craigburke.document.core.Font
import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.Margin
import com.craigburke.document.core.Text
import com.craigburke.document.core.TextBlock
import spock.lang.Specification

/**
 * Base test for all Render Elements
 * @author Craig Burke
 */
class RendererTestBase extends Specification {

    final float defaultLineHeight = 19f

    Document makeDocument() {
        new Document(margin: Document.defaultMargin, font: new Font())
    }

    TextBlock makeParagraph(TextBlock paragraph, BaseNode parent = makeDocument()) {
        TextBlock newParagraph = paragraph.clone()
        newParagraph.parent = parent
        parent.children << newParagraph
        newParagraph
    }

    TextBlock makeParagraph(int lineCount, BaseNode parent = makeDocument()) {
        TextBlock paragraph = new TextBlock(margin: Margin.NONE, font: new Font())
        lineCount.times {
            paragraph.children << new Text(value: "Line${it}", font: new Font())
            if (it != lineCount - 1) {
                paragraph.children << new LineBreak()
            }
        }
        paragraph.parent = parent
        parent.children << paragraph
        paragraph
    }

    ParagraphRenderer makeParagraphElement(TextBlock paragraph, Document document = makeDocument()) {
        PdfDocument pdfDocument = new PdfDocument(document)
        float width = paragraph.parent.width
        new ParagraphRenderer(paragraph, pdfDocument, 0f, width)
    }

}
