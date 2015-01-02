package com.craigburke.document.builder

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

import java.awt.Color

@InheritConstructors
class PdfDocumentBuilder extends DocumentBuilder {

	private PdfWriter writer
	
	Document createDocument(Document document, OutputStream out) {
		document.item = new PdfDocument()

		document.item.setMargins(document.marginLeft, document.marginRight, document.marginTop, document.marginBottom)

		writer = PdfWriter.getInstance(document.item as PdfDocument, out)
		document.item.addHeader('marginTop', "${document.marginTop}")
		document.item.addHeader('marginRight', "${document.marginRight}")
		document.item.addHeader('marginBottom', "${document.marginBottom}")
		document.item.addHeader('marginLeft', "${document.marginLeft}")

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

		pdfParagraph.indentationLeft = paragraph.marginLeft as Float
		pdfParagraph.indentationRight = paragraph.marginRight as Float
		pdfParagraph.spacingAfter = paragraph.marginBottom as Float
		pdfParagraph.leading = paragraph.marginTop

		paragraph.item = pdfParagraph
	}
	
	void addImageToParagraph(Image image, Paragraph paragraph) {
	    PdfImage img = PdfImage.getInstance(image.data)
		paragraph.item.add(img)
	}
	
	void addLineBreakToParagraph(Paragraph paragraph) {
	
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
		table.item = new PdfPTable(table.columns)
		table.item.totalWidth = table.width
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
			table.item.addCell(cell.item)
		}
		table.item.completeRow()
	}
	
	def onCellComplete = { Cell cell, Row row -> 
		def phrase = new Phrase()
		cell.item.each { chunk ->
			phrase.add(chunk)
		}
		row.item << new PdfPCell(phrase)
	}
	
	void write(Document document, OutputStream out) {
		if (document.item.pageNumber == 0) {
			// Add a blank page if no content has been added
			writer.pageEmpty = false
			document.item.newPage()
		}
		document.item.close()
	}

	private static Chunk getTextChunk(Font font, String text) {
		def textFont = FontFactory.getFont(font.family, font.size)
		textFont.color = font.rgbColor as Color
	    new Chunk(text ?: "", textFont)
	}

}