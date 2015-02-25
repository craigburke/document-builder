package com.craigburke.document.builder

import static com.craigburke.document.core.UnitUtil.pointToTwip
import static com.craigburke.document.core.UnitUtil.pointToEigthPoint

import com.craigburke.document.core.Align
import com.craigburke.document.core.LineBreak
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

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.util.Units
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder

/**
 * Builder for Word documents
 * @author Craig Burke
 */
@InheritConstructors
class WordDocumentBuilder extends DocumentBuilder {

	void initializeDocument(Document document, OutputStream out) {
		document.item = new XWPFDocument()

		def documentMargin = document.item.document.body.addNewSectPr().addNewPgMar()

		documentMargin.setLeft(pointToTwip(document.margin.left).intValue())
		documentMargin.setTop(pointToTwip(document.margin.top).intValue())
		documentMargin.setRight(pointToTwip(document.margin.right).intValue())
		documentMargin.setBottom(pointToTwip(document.margin.bottom).intValue())
    }

	def addParagraphToDocument = { Paragraph paragraph, Document document ->
        paragraph.item = document.item.createParagraph()
		setParagraphProperties(paragraph)
	}

	def addParagraphToCell = { Paragraph paragraph, Cell cell ->
		def firstParagraph = cell.item.paragraphs[0]

		paragraph.item = firstParagraph.isEmpty() ? firstParagraph : cell.item.addParagraph()
		setParagraphProperties(paragraph)
	}

	private void setParagraphProperties(Paragraph paragraph) {
		paragraph.item.with {
			spacingAfter = pointToTwip(paragraph.margin.bottom)
			spacingBefore = pointToTwip(paragraph.margin.top)
			spacingAfterLines = paragraph.lineHeight
		}

		if (paragraph.align == Align.RIGHT) {
			paragraph.item.alignment = ParagraphAlignment.RIGHT
		}
		else if (paragraph.align == Align.CENTER) {
			paragraph.item.alignment = ParagraphAlignment.CENTER
		}

		def indent = paragraph.item.CTP.PPr.addNewInd()
		indent.left = pointToTwip(paragraph.margin.left)
		indent.right = pointToTwip(paragraph.margin.right)
	}

	def addTextToParagraph = { Text text, Paragraph paragraph ->
        addTextRun(paragraph.item, text)
	}

	def addImageToParagraph = { Image image, Paragraph paragraph ->
		addImageRun(paragraph.item, image)
	}

	def addLineBreakToParagraph = { LineBreak lineBreak, Paragraph paragraph ->
		def run = paragraph.item.createRun()
		run.addBreak()
	}

	def addTableToDocument = { Table table, Document document ->
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

	def addRowToTable = { Row row, Table table ->
		row.item = (row.position == 0) ? table.item.getRow(0) : table.item.createRow()
	}

	def addCellToRow = { Cell cell, Row row ->
        Table table = row.parent
		cell.item = row.item.getCell(cell.position)

		def cellProperties = cell.item.CTTc.addNewTcPr()
		def padding = cellProperties.addNewTcMar()

		padding.addNewTop().w = pointToTwip(table.padding)
		padding.addNewBottom().w = pointToTwip(table.padding)
		padding.addNewLeft().w = pointToTwip(table.padding)
		padding.addNewRight().w = pointToTwip(table.padding)

		if (cell.width) {
			cellProperties.addNewTcW().w = pointToTwip(cell.width)
		}
	}

	private fixParagraphMargins(items) {
		items.eachWithIndex { child, index ->
			if (index > 0) {
				def previousChild = items[index - 1]
				if (child.getClass() == Paragraph && previousChild.getClass() == Paragraph) {
					previousChild.item.spacingAfter += child.item.spacingBefore
				}
			}
		}
	}

	void writeDocument(Document document, OutputStream out) {
		fixParagraphMargins(document.children)
		document.item.write(out)
	}

	private void addTextRun(paragraph, Text textNode) {
		Font font = textNode.font

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
            color = font.color.hex
            bold = font.bold
            italic = font.italic
            text = textNode.value
        }

	}

	private static void addImageRun(paragraph, Image image) {
        def run = paragraph.createRun()

        InputStream pictureData = new ByteArrayInputStream(image.data)
        int width = Units.toEMU(image.width)
        int height = Units.toEMU(image.height)

	    run.addPicture(pictureData, XWPFDocument.PICTURE_TYPE_PNG, image.name, width, height)
	}

}
