package com.craigburke.document.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.DocumentBuilderSpec

class WordDocumentBuilderSpec extends DocumentBuilderSpec  {

	DocumentBuilder createBuilderInstance(OutputStream out) {
		new WordDocumentBuilder(out)
	}

	Document getDocument(byte[] data) {
		WordDocumentLoader.load(data)
	}
}