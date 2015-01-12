package com.craigburke.document.builder

import com.lowagie.text.xml.xmp.XmpWriter
import groovy.transform.InheritConstructors

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.Document
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Table
import com.craigburke.document.core.Row
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Image
import com.craigburke.document.core.Text
import com.craigburke.document.core.Font

import com.lowagie.text.Document as PdfDocument
import com.lowagie.text.Paragraph as PdfParagraph
import com.lowagie.text.Image as PdfImage
import com.lowagie.text.Chunk
import com.lowagie.text.Phrase

import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfWriter
import com.lowagie.text.FontFactory
import groovy.xml.MarkupBuilder

import java.awt.Color

@InheritConstructors
class PdfDocumentBuilder extends DocumentBuilder {

	private PdfWriter writer
	
	Document createDocument(Document document, OutputStream out) {
		document.item = new PdfDocument()

		document.item.setMargins(document.margin.left, document.margin.right, document.margin.top, document.margin.bottom)

		writer = PdfWriter.getInstance(document.item as PdfDocument, out)

		document.item.open()
		document
	}

	void addFontFolder(File folder) {
	 	FontFactory.registerDirectory(folder.path)
	}
	
	void addFont(File font) {
		FontFactory.register(font.path)
	}
		
	void addParagraphToDocument(Paragraph paragraph, Document document) {
		def pdfParagraph = new PdfParagraph()

		pdfParagraph.indentationLeft = paragraph.margin.left as Float
		pdfParagraph.indentationRight = paragraph.margin.right as Float
		pdfParagraph.spacingAfter = paragraph.margin.bottom as Float
		pdfParagraph.leading = paragraph.margin.top as Float

		paragraph.item = pdfParagraph
	}
	
	void addImageToParagraph(Image image, Paragraph paragraph) {
	    PdfImage img = PdfImage.getInstance(image.data)
		paragraph.item.add(img)
	}
	
	void addLineBreakToParagraph(Paragraph paragraph) {
		paragraph.item.add(Chunk.NEWLINE)
	}
	
	void addTextToParagraph(Text text, Paragraph paragraph) {
		Chunk chunk = getTextChunk(text.font, text.value)
		paragraph.item.add(chunk)
	}
	
	def onParagraphComplete = { Paragraph paragraph ->
		def parent = paragraph.parent
		
		if (parent instanceof Document) {
			document.item.add(paragraph.item)
		}
		else {
			parent.item.addElement(paragraph.item)
		}
	}

	void addTableToDocument(Table table, Document document) {
		PdfPTable pdfTable = new PdfPTable(table.columns)
		pdfTable.totalWidth = table.width
		pdfTable.spacingBefore = 0
		pdfTable.spacingAfter = 0

		table.item = pdfTable
	}

	void addRowToTable(Row row, Table table) {
		row.item = []
	}
	
	void addCellToRow(Cell cell, Row row) { 
		cell.item = new PdfPCell()
	}
	
	void addParagraphToCell(Paragraph paragraph, Cell cell) {
		paragraph.item = new PdfParagraph()
	}

	def onTableComplete = { Table table ->
		document.item.add(table.item)
	}
	
	def onRowComplete = { Row row, Table table -> 
		row.item.each { cell ->
			table.item.addCell(cell)
		}
		table.item.completeRow()
	}
	
	def onCellComplete = { Cell cell, Row row -> 
		def phrase = new Phrase()
		cell.paragraphs.each { paragraph ->
			phrase.add(paragraph.item)
		}
		row.item << new PdfPCell(phrase)
	}
	
	void write(Document document, OutputStream out) {
		if (document.item.pageNumber == 0) {
			// Add a blank page if no content has been added
			writer.pageEmpty = false
			document.item.newPage()
		}

		addMetadata()

		document.item.close()
	}

	private void addMetadata() {
		ByteArrayOutputStream xmpOut = new ByteArrayOutputStream()
		XmpWriter xmpWriter = new XmpWriter(xmpOut)

		def xmlWriter = new StringWriter()
		def xml = new MarkupBuilder(xmlWriter)

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
					table(columns: child.columns, width: child.width, borderSize: child.borderSize) {
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

		xmpWriter.addRdfDescription("", xmlWriter.toString())
		xmpWriter.close()
		writer.xmpMetadata = xmpOut.toByteArray()
	}

	private static Chunk getTextChunk(Font font, String text) {
		def textFont = FontFactory.getFont(font.family, font.size)
		textFont.color = font.rgbColor as Color
	    new Chunk(text ?: "", textFont)
	}

}