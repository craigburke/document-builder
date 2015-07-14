package com.craigburke.document.builder

import com.craigburke.document.core.Document
import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.test.BaseBuilderSpec

/**
 * WordDocument tests
 * @author Craig Burke
 */
class WordDocumentBuilderSpec extends BaseBuilderSpec {

    DocumentBuilder getBuilderInstance(OutputStream out) {
        new WordDocumentBuilder(out)
    }

    Document getDocument(byte[] data) {
        WordDocumentLoader.load(data)
    }
}
