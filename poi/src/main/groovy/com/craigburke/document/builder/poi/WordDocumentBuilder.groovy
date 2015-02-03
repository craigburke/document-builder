package com.craigburke.document.builder.poi

import com.craigburke.document.core.Align
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
import org.apache.poi.xwpf.usermodel.ParagraphAlignment

import static com.craigburke.document.core.UnitUtil.*

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.util.Units
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder

@InheritConstructors
class WordDocumentBuilder extends DocumentBuilder {

	void createDocument(Document document, OutputStream out) {
		document.item = new XWPFDocument()

		def documentMargin = document.item.document.body.addNewSectPr().addNewPgMar()
				
		documentMargin.setLeft(pointToTwip(document.margin.left).intValue())
		documentMargin.setTop(pointToTwip(document.margin.top).intValue())
		documentMargin.setRight(pointToTwip(document.margin.right).intValue())
		documentMargin.setBottom(pointToTwip(document.margin.bottom).intValue())
    }

	void addParagraphToDocument(Paragraph paragraph, Document document) {
        paragraph.item = document.item.createParagraph()
		setParagraphProperties(paragraph)
	}
	
	void addParagraphToCell(Paragraph paragraph, Cell cell) {
		def firstParagraph = cell.item.paragraphs[0]

		paragraph.item = firstParagraph.isEmpty() ? firstParagraph : cell.item.addParagraph()
		setParagraphProperties(paragraph)
	}

	private void setParagraphProperties(Paragraph paragraph) {
		
		paragraph.item.with {
			spacingAfter = pointToTwip(paragraph.margin.bottom)
			spacingBefore = pointToTwip(paragraph.margin.top)
			spacingAfterLines = paragraph.leading
		}

		if (paragraph.align == Align.RIGHT) {
			paragraph.item.alignment = ParagraphAlignment.RIGHT
		}
		else if (paragraph.align == Align.CENTER) {
			paragraph.item.alignment = ParagraphAlignment.CENTER
		}
		else if (paragraph.align == Align.JUSTIFY) {
			paragraph.item.alignment = ParagraphAlignment.BOTH
		}

		def indent = paragraph.item.CTP.PPr.addNewInd()
		indent.left = pointToTwip(paragraph.margin.left)
		indent.right = pointToTwip(paragraph.margin.right)
	}
	
	void addTextToParagraph(Text text, Paragraph paragraph) {
        createTextRun(paragraph.item, text)
	}
	
	void addImageToParagraph(Image image, Paragraph paragraph) {
		createImageRun(paragraph.item, image)
	}
	
	void addLineBreakToParagraph(Paragraph paragraph) {
		def run = paragraph.item.createRun()
		run.addBreak()
	}

	void addTableToDocument(Table table, Document document) {
		table.item = document.item.createTable(1, table.columns)		
		
		def tableProperties = table.item.CTTbl.tblPr

		if (table.width) {
			tableProperties.tblW.w = pointToTwip(table.width)
		}
		
		def tableBorder = tableProperties.tblBorders
		def properties = ['top', 'right', 'bottom', 'left', 'insideH', 'insideV']

		properties.each { property ->
			def tableBorderSection = tableBorder."${property}"

			tableBorderSection.sz = pointToEigthPoint(table.border.size)
			tableBorderSection.color = table.border.color.hex
			tableBorderSection.val = table.border.size == 0 ? STBorder.NONE : STBorder.SINGLE
		}

	}
	
	void addRowToTable(Row row, Table table) {
		row.item = (row.position == 0) ? table.item.getRow(0) : table.item.createRow()
	}
	
	void addCellToRow(Cell cell, Row row) {
		cell.item = row.item.getCell(cell.position)

		def cellProperties = cell.item.CTTc.addNewTcPr()
		def padding = cellProperties.addNewTcMar()
		
		padding.addNewTop().w = pointToTwip(cell.padding)
		padding.addNewBottom().w = pointToTwip(cell.padding)
		padding.addNewLeft().w = pointToTwip(cell.padding)
		padding.addNewRight().w = pointToTwip(cell.padding)

		if (cell.width) {
			cellProperties.addNewTcW().w = pointToTwip(cell.width)
		}
	}

	private fixParagraphMargins(items) {
		items.eachWithIndex { child, index ->
			if (index > 0) {
				def previousChild = items[index - 1]
				if (child instanceof Paragraph && previousChild instanceof Paragraph) {
					previousChild.item.spacingAfter += child.item.spacingBefore
				}
			}
		}
	}

	void write(Document document, OutputStream out) {
		fixParagraphMargins(document.children)
		document.item.write(out)
	}
	
	private void createTextRun(paragraph, Text text) {
		Font font = text.font
		
		def run 
		def currentRuns = paragraph.runs
		
		if (currentRuns && !currentRuns.first().toString()) {
			// Just grab the first run if it doesn't have any content
			run = currentRuns.first()
		}
		else {
			run = paragraph.createRun()
		}
		
		run.fontFamily = font.family
		run.fontSize = font.size
		run.color = font.color.hex
		run.bold = font.bold
		run.italic = font.italic
		run.text = text.value

	}
	
	private static void createImageRun(paragraph, Image image) {
        def run = paragraph.createRun()
	    run.addPicture(new ByteArrayInputStream(image.data), XWPFDocument.PICTURE_TYPE_PNG, image.name, Units.toEMU(image.width), Units.toEMU(image.height))
	}
	
}