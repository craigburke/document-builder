package com.craigburke.document.builder

import static com.craigburke.document.core.UnitUtil.pointToTwip
import static com.craigburke.document.core.UnitUtil.pointToEigthPoint

import com.craigburke.document.core.HeaderFooterOptions
import com.craigburke.document.core.PageBreak
import org.apache.poi.xwpf.usermodel.BreakType

import com.craigburke.document.core.builder.RenderState
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy
import org.apache.poi.xwpf.usermodel.XWPFHeader
import org.apache.poi.xwpf.usermodel.XWPFParagraph

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
import org.apache.poi.xwpf.usermodel.XWPFRun
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

	def addParagraphToCell = { Paragraph paragraph, Cell cell ->
		def firstParagraph = cell.item.paragraphs[0]

		paragraph.item = firstParagraph.isEmpty() ? firstParagraph : cell.item.addParagraph()
		setParagraphProperties(paragraph)
	}

	private void setParagraphProperties(Paragraph paragraph) {
		paragraph.item.with {
			spacingAfter = pointToTwip(paragraph.margin.bottom)
			spacingBefore = pointToTwip(paragraph.margin.top)
			spacingAfterLines = paragraph.textHeight
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

	private XWPFHeaderFooterPolicy getHeaderFooterPolicy() {
		document.item.headerFooterPolicy ?: new XWPFHeaderFooterPolicy(document.item)
	}

	def onParagraphComplete = { Paragraph paragraph ->
		switch (renderState) {
			case RenderState.PAGE:
				paragraph.item = paragraph.item ?: document.item.createParagraph()
				break

			case RenderState.HEADER:
				XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT)
				paragraph.item = header.paragraphs[0]
				break

			case RenderState.FOOTER:
				def footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT)
				paragraph.item = footer.paragraphs[0]
				break
		}

		setParagraphProperties(paragraph)

		paragraph.children.eachWithIndex { child, index ->
			int previousLineBreaks = getPreviousLineBreakCount(paragraph.children, index)

			switch (child.getClass()) {
				case Text:
					addTextRun(paragraph.item, child, previousLineBreaks)
					break
				case Image:
					addImageRun(paragraph.item, child, previousLineBreaks)
					break
			}
		}
	}

	private int getPreviousLineBreakCount(List items, int currentIndex) {
		int itemIndex = currentIndex - 1
		def item = items[itemIndex]
		int count = 0
		while (itemIndex >= 0 && item instanceof LineBreak) {
			count++
			itemIndex--
			item = items[itemIndex]
		}
		count
	}

	def addPageBreakToDocument = { PageBreak pageBreak, Document document ->
		XWPFParagraph paragraph = document.item.createParagraph()
		XWPFRun run = paragraph.createRun()
		run.addBreak(BreakType.PAGE)
	}

	def addTableToDocument = { Table table, Document document ->
		table.item = document.item.createTable(1, table.columns)
	}

	def addRowToTable = { Row row, Table table ->
		row.item = (row.position == 0) ? table.item.getRow(0) : table.item.createRow()
	}

	def addCellToRow = { Cell cell, Row row ->
		cell.item = row.item.getCell(cell.position)
	}

	def onTableComplete = { Table table ->
		def tableProperties = table.item.CTTbl.tblPr
		tableProperties.tblW.w = pointToTwip(table.width)

		def tableBorder = tableProperties.tblBorders
		def properties = ['top', 'right', 'bottom', 'left', 'insideH', 'insideV']

		properties.each { property ->
			def tableBorderSection = tableBorder."${property}"

			tableBorderSection.sz = pointToEigthPoint(table.border.size)
			tableBorderSection.color = table.border.color.hex
			tableBorderSection.val = table.border.size == 0 ? STBorder.NONE : STBorder.SINGLE
		}

		table.children.each { Row row ->
			row.children.each { Cell cell ->
				def cellProperties = cell.item.CTTc.addNewTcPr()
				cellProperties.addNewTcW().w = pointToTwip(cell.width - (table.padding * 2))

				def padding = cellProperties.addNewTcMar()

				padding.addNewTop().w = pointToTwip(table.padding)
				padding.addNewBottom().w = pointToTwip(table.padding)
				padding.addNewLeft().w = pointToTwip(table.padding)
				padding.addNewRight().w = pointToTwip(table.padding)
			}
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
		addHeaderFooter()
		fixParagraphMargins(document.children)
		document.item.write(out)
	}

	private void addHeaderFooter() {
		def options = new HeaderFooterOptions(pageNumber:'#pageNumber#', pageCount:'#pageCount##', dateGenerated:new Date())

		if (document.header) {
			renderState = RenderState.HEADER
			document.header(options)
		}
		if (document.footer) {
			renderState = RenderState.FOOTER
			document.footer(options)
		}

		renderState = RenderState.PAGE
	}

	private void addTextRun(paragraph, Text textNode, int startingLineBreaks) {
		Font font = textNode.font

		XWPFRun run
		def currentRuns = paragraph.runs

		if (currentRuns && !currentRuns.first().toString()) {
			// Just grab the first run if it doesn't have any content
			run = currentRuns.first()
		}
		else {
			run = paragraph.createRun()
		}

		startingLineBreaks.times {
			run.addBreak()
		}

		run.with {
            fontFamily = font.family
            fontSize = font.size
            color = font.color.hex
            bold = font.bold
            italic = font.italic
            text = textNode.value
        }

		run
	}

	private static void addImageRun(paragraph, Image image, int startingLineBreaks) {
		XWPFRun run = paragraph.createRun()

		startingLineBreaks.times {
			run.addBreak()
		}

        InputStream pictureData = new ByteArrayInputStream(image.data)
        int width = Units.toEMU(image.width)
        int height = Units.toEMU(image.height)

	    run.addPicture(pictureData, XWPFDocument.PICTURE_TYPE_PNG, image.name, width, height)
		run
	}

}
