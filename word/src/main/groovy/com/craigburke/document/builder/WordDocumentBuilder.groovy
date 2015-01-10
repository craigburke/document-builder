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

import static com.craigburke.document.core.UnitUtil.*

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.util.Units
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder

@InheritConstructors
class WordDocumentBuilder extends DocumentBuilder {

	Document createDocument(Document document, OutputStream out) {
		document.item = new XWPFDocument()

		def documentMargin = document.item.document.body.addNewSectPr().addNewPgMar()
				
		documentMargin.setLeft(pointToTwip(document.marginLeft).intValue())
		documentMargin.setTop(pointToTwip(document.marginTop).intValue())
		documentMargin.setRight(pointToTwip(document.marginRight).intValue())
		documentMargin.setBottom(pointToTwip(document.marginBottom).intValue())
		
		document
	}
		
	void addParagraphToDocument(Paragraph paragraph, Document document) {
        paragraph.item = document.item.createParagraph()
		setParagraphProperties(paragraph)
	}
	
	void addParagraphToCell(Paragraph paragraph, Cell cell) {
		def firstParagraph = cell.item.paragraphs[0]
        paragraph.item = isParagraphEmpty(firstParagraph) ? firstParagraph : cell.item.addParagraph()
		setParagraphProperties(paragraph)
	}
	
	private void setParagraphProperties(Paragraph paragraph) {		
		paragraph.item.with {
			spacingAfter = pointToTwip(paragraph.marginBottom)
			spacingBefore = pointToTwip(paragraph.marginTop)
		}
		
		def indent = paragraph.item.CTP.PPr.addNewInd()
		indent.left = pointToTwip(paragraph.marginLeft)
		indent.right = pointToTwip(paragraph.marginRight)
	}
	
	void addTextToParagraph(Text text, Paragraph paragraph) {
        createTextRun(paragraph.item, text.font, text.value)
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
		
		if (table.borderSize != null) {
			def tableBorder = tableProperties.tblBorders
			def properties = ['top', 'right', 'bottom', 'left', 'insideH', 'insideV']
			
			properties.each { property ->
				def tableBorderSection = tableBorder."${property}"
								
				tableBorderSection.sz = pointToEigthPoint(table.borderSize)
				tableBorderSection.color = "auto"
				tableBorderSection.val = table.borderSize == 0 ? STBorder.NONE : STBorder.SINGLE
			}
		}
	}
	
	void addRowToTable(Row row, Table table) {
		row.item = (row.position == 0) ? table.item.getRow(0) : table.item.createRow()
	}
	
	void addCellToRow(Cell cell, Row row) {
		cell.item = row.item.getCell(cell.position)
		
		if (cell.width) {
			cell.item.CTTc.addNewTcPr().addNewTcW().w = pointToTwip(cell.width)
		}
	}
	
	void write(Document document, OutputStream out) {
		document.item.write(out)
	}
	
	private static boolean isParagraphEmpty(paragraph) {
		!(paragraph.runs.find { it.toString() })
	}
	
	private void createTextRun(paragraph, Font font, String runText) {
		def run 
		def currentRuns = paragraph.runs
		
		if (currentRuns && !currentRuns.first().toString()) {
			// Just grab the first run if it doesn't have any content
			run = currentRuns.first()
		}
		else {
			run = paragraph.createRun()
		}
		
		run.with {
			fontFamily = font.family
			fontSize = font.size
			color = font.hexColor
			bold = font.bold
			italic = font.italic
			text = runText
		}		

	}
	
	private static void createImageRun(paragraph, Image image) {
        def run = paragraph.createRun()
	    run.addPicture(new ByteArrayInputStream(image.data), XWPFDocument.PICTURE_TYPE_PNG, image.name, Units.toEMU(image.width), Units.toEMU(image.height))
	}


	
}