package com.craigburke.document.core.builder

import com.craigburke.document.core.BackgroundAssignable
import com.craigburke.document.core.BaseNode
import com.craigburke.document.core.BlockNode
import com.craigburke.document.core.EmbeddedFont
import com.craigburke.document.core.Heading
import com.craigburke.document.core.Linkable
import com.craigburke.document.core.Stylable
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
	protected List<String> imageFileNames = []

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

		if (node instanceof Stylable) {
			setNodeFont(node, nodeProperties)
		}
		if (node instanceof BlockNode) {
			setNodeMargins(node, nodeProperties)
		}
		if (node instanceof BackgroundAssignable) {
			setNodeBackground(node, nodeProperties)
		}
		if (node instanceof Linkable) {
			String parentUrl = (node.parent instanceof Linkable) ? node.parent.url : null
			node.url = node.url ?: parentUrl
		}
	}

	protected void setNodeFont(Stylable node, nodeProperties) {
		node.font = (node instanceof Document) ? new Font() : node.parent.font.clone()
		node.font.size = (node instanceof Heading) ? null : node.font.size
		nodeProperties.each {
			node.font << it.font
		}
		if (node instanceof Heading && !node.font.size) {
			node.font.size = document.font.size * Heading.FONT_SIZE_MULTIPLIERS[node.level - 1]
		}
	}

	protected void setNodeMargins(BlockNode node, nodeProperties) {
		node.margin = node.getClass().defaultMargin
		nodeProperties.each {
			node.margin << it.margin
		}
	}

	protected void setNodeBackground(BackgroundAssignable node, nodeProperties) {
		nodeProperties.each {
			node.backgroundColor = it.backgroundColor
		}
		if (!node.backgroundColor && node.parent instanceof BackgroundAssignable && node.parent.backgroundColor) {
			node.backgroundColor = "#${node.parent.backgroundColor.hex}"
		}
	}

	protected String[] getTemplateKeys(BaseNode node, String nodeKey) {
		def keys = [nodeKey]
		if (node instanceof Heading) {
			keys << "heading${node.level}"
		}
		if (node instanceof Stylable && node.style) {
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
		if (addEmbeddedFont) {
			addEmbeddedFont(embeddedFont)
		}
    }

    abstract void initializeDocument(Document document, OutputStream out)
	abstract void writeDocument(Document document, OutputStream out)

	def addPageBreakToDocument
	def onTextBlockComplete
	def onTableComplete
	def addEmbeddedFont

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
