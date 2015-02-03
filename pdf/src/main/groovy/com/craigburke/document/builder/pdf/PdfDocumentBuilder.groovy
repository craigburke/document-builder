package com.craigburke.document.builder.pdf

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
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font

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
		document.item.x += paragraph.margin.left
		document.item.y += paragraph.margin.top
	}

	void addImageToParagraph(Image image, Paragraph paragraph) {

	}
	
	void addLineBreakToParagraph(Paragraph paragraph) {
	
	}
	
	void addTextToParagraph(Text text, Paragraph paragraph) {
	    //
	}
	
	def onParagraphComplete = { Paragraph paragraph ->
        PdfDocument pdfDocument = document.item

        PDPageContentStream contentStream = document.item.contentStream

        contentStream.beginText()
        contentStream.moveTextPositionByAmount(pdfDocument.x, pdfDocument.translatedY - paragraph.leading)

        int xDiff = 0
        paragraph.children.each { Text text ->
            contentStream.moveTextPositionByAmount(xDiff, 0)

            PDFont font = PDType1Font.HELVETICA_BOLD

            def color = text.font.color.RGB
            contentStream.setNonStrokingColor(color[0], color[1], color[2])
            contentStream.setFont(font,text.font.size)
            contentStream.drawString(text.value)

            xDiff = font.getStringWidth(text.value) / 1000 * text.font.size

        }
        contentStream.endText()

        document.item.y += (paragraph.leading + paragraph.margin.bottom)
	    document.item.x = document.margin.left
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