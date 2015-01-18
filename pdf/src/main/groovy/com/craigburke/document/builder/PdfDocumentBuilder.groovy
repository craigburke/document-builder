package com.craigburke.document.builder

import com.craigburke.document.core.Align
import com.lowagie.text.Element
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

import com.lowagie.text.Document as iTextDocument
import com.lowagie.text.Paragraph as iTextParagraph
import com.lowagie.text.Image as iTextImage
import com.lowagie.text.Chunk

import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfWriter
import com.lowagie.text.FontFactory
import com.lowagie.text.Font as PdfFont
import groovy.xml.MarkupBuilder

import java.awt.Color

@InheritConstructors
class PdfDocumentBuilder extends DocumentBuilder {

	private PdfWriter writer
	
	Document createDocument(Document document, OutputStream out) {
		document.item = new iTextDocument()

		document.item.setMargins(document.margin.left, document.margin.right, document.margin.top, document.margin.bottom)
		writer = PdfWriter.getInstance(document.item as iTextDocument, out)
		writer.strictImageSequence = true
		
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
		def pdfParagraph = new iTextParagraph()

		pdfParagraph.indentationLeft = paragraph.margin.left as Float
		pdfParagraph.indentationRight = paragraph.margin.right as Float
		pdfParagraph.spacingBefore = paragraph.margin.top as Float
		pdfParagraph.spacingAfter = paragraph.margin.bottom as Float

		if (paragraph.align == Align.RIGHT) {
			paragraph.item.alignment = Element.ALIGN_RIGHT
		}
		else if (paragraph.align == Align.CENTER) {
			paragraph.item.alignment = Element.ALIGN_MIDDLE
		}
		else if (paragraph.align == Align.JUSTIFY) {
			paragraph.item.alignment = Element.ALIGN_JUSTIFIED
		}

		paragraph.item = pdfParagraph
	}
	
	void addImageToParagraph(Image image, Paragraph paragraph) {
		def img = iTextImage.getInstance(image.data)
		img.scaleAbsolute(image.width, image.height)
		paragraph.item.add(new Chunk(img, 0, 0, true))
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
		
		paragraph.item.setLeading(paragraph.leading, 0)
		paragraph.item.spacingBefore -= paragraph.leading

		// Dummy paragraph used to make sure spacingBefore on first paragraph renders correctly
		def dummyParagraph = new iTextParagraph(" ")
		dummyParagraph.setLeading(0)
		
		if (parent instanceof Document) {
			if (parent.children[0] == paragraph) {
				document.item.add(dummyParagraph)
			}
			document.item.add(paragraph.item)
		}
		else {
			if (parent.paragraphs[0] == paragraph) {
				parent.item.addElement(dummyParagraph)
			}
			parent.item.addElement(paragraph.item)
		}
	}

	void addTableToDocument(Table table, Document document) {
		// Create table in onTableComplete
	}

	void addRowToTable(Row row, Table table) {
		row.item = []
	}
	
	void addCellToRow(Cell cell, Row row) {
		PdfPCell pdfCell = new PdfPCell()

		pdfCell.padding = cell.padding
		pdfCell.border = row.parent.border.size
		pdfCell.borderColor = row.parent.border.color.RGB
		cell.item = pdfCell
	}
	
	void addTextToCell(Text text, Cell cell) {
		def chunk = getTextChunk(text.font, text.value)
		cell.item.addElement(chunk)
	}

	void addImageToCell(Image image, Cell cell) {
		def img = iTextImage.getInstance(image.data)
		img.scaleAbsolute(image.width, image.height)
		cell.item.addElement(new Chunk(img, 0, 0, true))
	}

	void addLineBreakToCell(Cell cell) {
		cell.item.addElement(Chunk.NEWLINE)
	}

	def onTableComplete = { Table table ->
		PdfPTable pdfTable = new PdfPTable(table.columns)

		pdfTable.lockedWidth = true
		pdfTable.totalWidth = table.width
		pdfTable.widths = getRelativeCellWidths(table)
		pdfTable.spacingBefore = 0
		pdfTable.spacingAfter = 0
		
		table.item = pdfTable
		
		table.rows.each { row ->
			row.item.each { cell ->
				table.item.addCell(cell)
			}
			table.item.completeRow()
		}

		document.item.add(table.item)
	}
	
	private int[] getRelativeCellWidths(Table table) {
		BigDecimal totalCellWidth = table.rows[0].cells.inject(0){ total, cell -> total + (cell.width ?: 0) }
		Integer remainingWidth = (table.width - totalCellWidth).intValueExact()
		table.rows[0].cells.collect { it.width?.intValueExact() ?: remainingWidth }
	}
	
	def onCellComplete = { Cell cell, Row row ->
		cell.children.each { child ->
			cell.item.addElement(child.item)
		}

		row.item << cell.item
	}


	private static Chunk getTextChunk(Font font, String text) {
		PdfFont textFont = FontFactory.getFont(font.family, font.size)
		textFont.color = font.color.RGB as Color

		if (font.bold && font.italic) {
			textFont.style = PdfFont.BOLDITALIC
		}
		else if (font.bold) {
			textFont.style = PdfFont.BOLD
		}
		else if (font.italic) {
			textFont.style = PdfFont.ITALIC
		}
		
		new Chunk(text ?: "", textFont)
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

		xmpWriter.addRdfDescription("", xmlWriter.toString())
		xmpWriter.close()
		writer.xmpMetadata = xmpOut.toByteArray()
	}

}