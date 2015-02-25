package com.craigburke.document.builder

import com.craigburke.document.core.Document

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.test.BaseBuilderSpec

/**
 * PdfDocument tests
 * @author Craig Burke
 */
class PdfDocumentBuilderSpec extends BaseBuilderSpec {

	DocumentBuilder getBuilderInstance(OutputStream out) {
		new PdfDocumentBuilder(out)
	}

	Document getDocument(byte[] data) {
		PdfDocumentLoader.load(data)
	}
}
