package com.craigburke.document.builder.pdf

import com.craigburke.document.core.Document

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.test.DocumentBuilderSpec

class PdfDocumentBuilderSpec extends DocumentBuilderSpec {

	DocumentBuilder createBuilderInstance(OutputStream out) {
		new PdfDocumentBuilder(out)
	}

	Document getDocument(byte[] data) {
		PdfDocumentLoader.load(data)
	}
}