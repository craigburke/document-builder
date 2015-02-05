package com.craigburke.document.builder.pdf

import com.craigburke.document.core.LineBreak
import groovy.transform.InheritConstructors
import groovy.xml.MarkupBuilder

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.Document
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Table
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Image
import com.craigburke.document.core.Text

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDMetadata

@InheritConstructors
class PdfDocumentBuilder extends DocumentBuilder {

	void createDocument(Document document, OutputStream out) {
        PdfDocument pdfDocument = new PdfDocument(document: new PDDocument())
        pdfDocument.addPage()
        document.item = pdfDocument

        pdfDocument.x = document.margin.left
        pdfDocument.y = document.margin.top

        document.item = pdfDocument
        document
    }
	
	void addParagraphToDocument(Paragraph paragraph, Document document) {
		document.item.x = paragraph.margin.left + document.margin.left
		document.item.y += paragraph.margin.top
	}

	void addImageToParagraph(Image image, Paragraph paragraph) {
        // Handle in onParagraphComplete
	}
	
	void addLineBreakToParagraph(LineBreak lineBreak, Paragraph paragraph) {
        // Handle in onParagraphComplete
	}
	
	void addTextToParagraph(Text text, Paragraph paragraph) {
        // Handle in onParagraphComplete
	}
	
	def onParagraphComplete = { Paragraph paragraph ->
        ParagraphRenderer paragraphRenderer = new ParagraphRenderer(paragraph, document)
        paragraphRenderer.render()
    }


	
	void addTableToDocument(Table table, Document document) {
	}

	void addRowToTable(Row row, Table table) {

    }
	
	void addCellToRow(Cell cell, Row row) {

	}
	
	void addParagraphToCell(Paragraph paragraph, Cell cell) {
	}
	

	void write(Document document, OutputStream out) {
		addMetadata()
		document.item.contentStream?.close()
		document.item.document.save(out)
		document.item.document.close()
	}

	private void addMetadata() {
		ByteArrayOutputStream xmpOut = new ByteArrayOutputStream()
		def xml = new MarkupBuilder(xmpOut.newWriter())

		xml.document(marginTop: "${document.margin.top}", marginBottom: "${document.margin.bottom}", marginLeft: "${document.margin.left}", marginRight: "${document.margin.right}") {

			delegate = xml
			resolveStrategy = Closure.DELEGATE_FIRST

			document.children.each { child ->
				if (child.getClass() == Paragraph) {
					paragraph(marginTop: "${child.margin.top}", marginBottom: "${child.margin.bottom}", marginLeft: "${child.margin.left}", marginRight: "${child.margin.right}") {
						child.children.findAll { it.getClass() == Image }.each {
							image()
						}
					}
				}
				else {
					table(columns: child.columns, width: child.width, borderSize: child.border.size) {
						child.rows.each {
							def cells = it.cells
							row() {
								cells.each {
									cell(width: "${it.width ?: 0}")
								}
							}
						}
					}
				}
			}
		}

		def catalog = document.item.document.documentCatalog
		PDMetadata metadata = new PDMetadata(document.item.document as PDDocument, new ByteArrayInputStream(xmpOut.toByteArray()), false)
		catalog.metadata = metadata
	}

}