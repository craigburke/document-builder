package com.craigburke.document.builder

/**
 * Enum for the various types of document parts a relationship can exist in
 * @author Craig Burke
 */
interface DocumentPartType {

    String getValue()
    String getFileName()
    String getContentType()
    String getRelationshipType()

}
