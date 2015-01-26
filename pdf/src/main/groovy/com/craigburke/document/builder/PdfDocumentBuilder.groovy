package com.craigburke.document.builder

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
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDMetadata
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font

@InheritConstructors
class PdfDocumentBuilder extends DocumentBuilder {

	private def pagePosition = [x: 0, y: 0]
	
	private PDPage currentPage
	private PDPageContentStream contentStream

	void createDocument(Document document, OutputStream out) {
		document.item = new PDDocument()
		currentPage = new PDPage()
		
		document.item.addPage(currentPage)
		pagePosition.x = document.margin.top
		pagePosition.y = document.margin.left
		
		contentStream = new PDPageContentStream(document.item as PDDocument, currentPage)
	}

	private int translateY(int y) {
		currentPage.mediaBox.height - y
	}
	
	
	void addParagraphToDocument(Paragraph paragraph, Document document) {
		pagePosition.x += paragraph.margin.left
		pagePosition.y += paragraph.margin.top
	}

	void addImageToParagraph(Image image, Paragraph paragraph) {
	
	}
	
	void addLineBreakToParagraph(Paragraph paragraph) {
	
	}
	
	void addTextToParagraph(Text text, Paragraph paragraph) {
		PDFont font = PDType1Font.HELVETICA_BOLD
		
		contentStream.beginText()
		contentStream.moveTextPositionByAmount(pagePosition.x, translateY(pagePosition.y))
		contentStream.setFont(font,text.font.size)
		contentStream.drawString(text.value)
		contentStream.endText()
		
		pagePosition.y += paragraph.leading
	}
	
	def onParagraphComplete = {Paragraph paragraph ->
		//pagePosition.y += paragraph.margin.bottom
	}
	
	void addTableToDocument(Table table, Document document) {
		// Create table in onTableComplete
	}

	void addRowToTable(Row row, Table table) {
	}
	
	void addCellToRow(Cell cell, Row row) {

	}
	
	void addParagraphToCell(Paragraph paragraph, Cell cell) {
	}
	

	void write(Document document, OutputStream out) {
		addMetadata()
		contentStream.close()
		document.item.save(out)
		document.item.close()
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

		def catalog = document.item.documentCatalog
		PDMetadata metadata = new PDMetadata(document.item as PDDocument, new ByteArrayInputStream(xmpOut.toByteArray()), false)
		catalog.metadata = metadata
	}

}