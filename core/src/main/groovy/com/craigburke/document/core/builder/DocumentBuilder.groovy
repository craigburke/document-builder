package com.craigburke.document.core.builder

import com.craigburke.document.core.BaseNode
import com.craigburke.document.core.BlockNode
import com.craigburke.document.core.EmbeddedFont
import com.craigburke.document.core.Heading
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
abstract class DocumentBuilder extends FactoryBuilderSupport {

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

	void setNodeProperties(BaseNode node, Map attributes, String nodeKey) {
		String[] templateKeys = getTemplateKeys(node, nodeKey)
		def nodeProperties = []

		templateKeys.each { String key ->
			if (document.template && document.template.containsKey(key)) {
				nodeProperties << document.template[key]
			}
		}
		nodeProperties << attributes

		if (node instanceof StyledNode) {
			setNodeFont(node, nodeProperties)
		}
		if (node instanceof BlockNode) {
			setNodeMargins(node, nodeProperties)
		}
	}

	protected void setNodeFont(StyledNode node, nodeProperties) {
		node.font = (node instanceof Document) ? new Font() : node.parent.font.clone()
		node.font.size = (node instanceof Heading) ? null : node.font.size
		nodeProperties.each { property ->
			node.font << property.font
		}
		if (node instanceof Heading && !node.font.size) {
			node.font.size = document.font.size * Heading.FONT_SIZE_MULTIPLIERS[node.level - 1]
		}
	}

	protected void setNodeMargins(BlockNode node, nodeProperties) {
		node.margin = node.getClass().DEFAULT_MARGIN
		nodeProperties.each { property ->
			node.margin << property.margin
		}
	}

	protected String[] getTemplateKeys(BaseNode node, String nodeKey) {
		def keys = [nodeKey]
		if (node instanceof Heading) {
			keys << "heading${node.level}"
		}
		if (node instanceof StyledNode && node.style) {
			keys << "${nodeKey}.${node.style}"
			if (node instanceof Heading) {
				keys << "heading${node.level}.${node.style}"
			}
		}
		keys
	}

    void addFont(Map params, String location) {
        EmbeddedFont embeddedFont = new EmbeddedFont(params)
        embeddedFont.file = new File(location)
        addFont(embeddedFont)
    }

    void addFont(EmbeddedFont embeddedFont) {
        document.embeddedFonts << embeddedFont
    }

    abstract void initializeDocument(Document document, OutputStream out)
	abstract void writeDocument(Document document, OutputStream out)

	def addPageBreakToDocument
	def onTextBlockComplete
	def onTableComplete
	def onRowComplete
	def onCellComplete

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
