package com.craigburke.document.builder

/**
 * Interface to describe various document parts such as document, styles or numbering XML files.
 * @author Vladimir Orany
 * @see BasicDocumentPartTypes
 */
interface DocumentPartType {

    String getValue()
    String getFileName()
    String getContentType()
    String getRelationshipType()

}
