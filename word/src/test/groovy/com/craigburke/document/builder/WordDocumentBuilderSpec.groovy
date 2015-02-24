package com.craigburke.document.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.test.DocumentBuilderSpec

/**
 * WordDocument tests
 * @author Craig Burke
 */
class WordDocumentBuilderSpec extends DocumentBuilderSpec  {

	DocumentBuilder getBuilderInstance(OutputStream out) {
		new WordDocumentBuilder(out)
	}

	Document getDocument(byte[] data) {
		WordDocumentLoader.load(data)
	}
}
