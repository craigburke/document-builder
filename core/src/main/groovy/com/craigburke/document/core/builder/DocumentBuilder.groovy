package com.craigburke.document.core.builder

import com.craigburke.document.core.BlockNode
import com.craigburke.document.core.EmbeddedFont
import com.craigburke.document.core.Margin
import com.craigburke.document.core.StyledNode
import com.craigburke.document.core.UnitCategory

import com.craigburke.document.core.factory.CreateFactory
import com.craigburke.document.core.factory.DocumentFactory
import com.craigburke.document.core.factory.HeadingFactory
import com.craigburke.document.core.factory.PageBreakFactory
import com.craigburke.document.core.factory.ParagraphFactory
import com.craigburke.document.core.factory.LineBreakFactory
import com.craigburke.document.core.factory.ImageFactory
import com.craigburke.document.core.factory.TextFactory
import com.craigburke.document.core.factory.TableFactory
import com.craigburke.document.core.factory.RowFactory
import com.craigburke.document.core.factory.CellFactory

import com.craigburke.document.core.Document
import com.craigburke.document.core.Font

/**
 * Document Builder base class
 * @author Craig Burke
 */
abstract class DocumentBuilder extends FactoryBuilderSupport implements TextBlockBuilder, TableBuilder {

	Document document
	OutputStream out
	RenderState renderState = RenderState.PAGE

	DocumentBuilder(OutputStream out) {
		super(true)
		this.out = out
	}

    DocumentBuilder(File file) {
        super(true)
        this.out = new FileOutputStream(file)
    }

	Font getFont() {
		current.font
	}

	def invokeMethod(String name, args) {
		use(UnitCategory) {
			super.invokeMethod(name, args)
		}
	}

	void setStyles(StyledNode node) {
		node.font = (node instanceof Document) ? new Font() : node.parent.font.clone()
		document.applyStyles(node)

		if (node instanceof BlockNode) {
			Margin defaultMargin = node.getClass().DEFAULT_MARGIN
			node.margin.setDefaults(defaultMargin)
		}
	}

    void addFont(Map params, String location) {
        EmbeddedFont embeddedFont = new EmbeddedFont(params)
        embeddedFont.file = new File(location)
        addFont(embeddedFont)
    }

    void addFont(EmbeddedFont embeddedFont) {
        document.embeddedFonts << embeddedFont
    }

	def addPageBreakToDocument
    abstract void initializeDocument(Document document, OutputStream out)
	abstract void writeDocument(Document document, OutputStream out)

	def registerObjectFactories() {
		registerFactory('create', new CreateFactory())
		registerFactory('document', new DocumentFactory())
		registerFactory('pageBreak', new PageBreakFactory())
		registerFactory('paragraph', new ParagraphFactory())
		registerFactory('lineBreak', new LineBreakFactory())
		registerFactory('image', new ImageFactory())
		registerFactory('text', new TextFactory())
		registerFactory('table', new TableFactory())
		registerFactory('row', new RowFactory())
		registerFactory('cell', new CellFactory())
		registerFactory('heading1', new HeadingFactory())
		registerFactory('heading2', new HeadingFactory())
		registerFactory('heading3', new HeadingFactory())
		registerFactory('heading4', new HeadingFactory())
		registerFactory('heading5', new HeadingFactory())
		registerFactory('heading6', new HeadingFactory())
	}
}

enum RenderState {
	PAGE, HEADER, FOOTER
}
