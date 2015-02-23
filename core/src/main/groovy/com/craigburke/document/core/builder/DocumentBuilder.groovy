package com.craigburke.document.core.builder

import com.craigburke.document.core.EmbeddedFont
import com.craigburke.document.core.UnitCategory
import com.craigburke.document.core.factory.*
import com.craigburke.document.core.Document
import com.craigburke.document.core.Font

abstract class DocumentBuilder extends FactoryBuilderSupport implements ParagraphBuilder, TableBuilder {
	
	Document document
	OutputStream out

	DocumentBuilder(OutputStream out) {
		super(true)
		this.out = out
	}
	
	Font getFont() {
		current.font
	}
	
	DocumentBuilder(File file) {
		super(true)
		this.out = new FileOutputStream(file)
	}

	def invokeMethod(String name, args) {
		use(UnitCategory) {
			return super.invokeMethod(name, args)
		}
	}

    void addFont(Map params, String location) {
        EmbeddedFont embeddedFont = new EmbeddedFont(params)
        embeddedFont.file = new File(location)
        addFont(embeddedFont)
    }

    abstract void addFont(EmbeddedFont embeddedFont)
	abstract void createDocument(Document document, OutputStream out)
	abstract void writeDocument(Document document, OutputStream out)

	def registerObjectFactories() {
		registerFactory("create", new CreateFactory())
		registerFactory("document", new DocumentFactory())
		registerFactory("paragraph", new ParagraphFactory())
		registerFactory("lineBreak", new LineBreakFactory())
		registerFactory("image", new ImageFactory())
		registerFactory("text", new TextFactory())
		registerFactory("table", new TableFactory())
		registerFactory("row", new RowFactory())
		registerFactory("cell", new CellFactory())
	}
}