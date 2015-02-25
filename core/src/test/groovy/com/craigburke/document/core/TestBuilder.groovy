package com.craigburke.document.core

import com.craigburke.document.core.builder.DocumentBuilder
import groovy.transform.InheritConstructors

/**
 * Basic implementation of a document builder for testing
 * @author Craig Burke
 */
@InheritConstructors
class TestBuilder extends DocumentBuilder {
    @Override
    void initializeDocument(Document document, OutputStream out) { }

    @Override
    void writeDocument(Document document, OutputStream out) { }
}
