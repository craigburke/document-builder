package com.craigburke.document.builder

import com.craigburke.document.core.Document

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.test.DocumentBuilderSpec

/**
 * PdfDocument tests
 * @author Craig Burke
 */
class PdfDocumentBuilderSpec extends DocumentBuilderSpec {

	DocumentBuilder getBuilderInstance(OutputStream out) {
		new PdfDocumentBuilder(out)
	}

	Document getDocument(byte[] data) {
		PdfDocumentLoader.load(data)
	}
}
