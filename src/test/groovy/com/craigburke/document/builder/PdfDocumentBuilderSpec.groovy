package com.craigburke.document.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Image
import com.craigburke.document.core.Table
import com.craigburke.document.core.Text
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper
import com.lowagie.text.xml.xmp.XmpReader

import static com.craigburke.document.core.UnitUtil.*

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.builder.PdfDocumentBuilder
import com.craigburke.document.DocumentBuilderSpec

import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage

class PdfDocumentBuilderSpec extends DocumentBuilderSpec {

	DocumentBuilder createBuilderInstance(OutputStream out) {
		new PdfDocumentBuilder(out)
	}

	Document getDocument(byte[] data) {
		PdfDocumentLoader.load(data)
	}
}