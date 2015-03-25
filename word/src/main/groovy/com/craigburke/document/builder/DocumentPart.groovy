package com.craigburke.document.builder

/**
 * OOXML Document Part
 * @author Craig Burke
 */
class DocumentPart {
    DocumentPartType type
    List<Relationship> relationships = []
    List images = []
}
