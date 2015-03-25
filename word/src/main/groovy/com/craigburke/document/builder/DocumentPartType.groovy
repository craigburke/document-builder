package com.craigburke.document.builder

/**
 * Enum for the various document parts a relationship can exit in
 * @author Craig Burke
 */
enum DocumentPartType {
    ROOT('root', '_rels/.rels'),
    DOCUMENT('document', 'word/_rels/document.xml.rels'),
    HEADER('header', 'word/_rels/header.xml.rels'),
    FOOTER('footer', 'word/_rels/footer.xml.rels')
    
    final String value
    final String relationshipFileLocation
    
    DocumentPartType(String value, String relationshipFileLocation) {
        this.value = value
        this.relationshipFileLocation = relationshipFileLocation
    }
}
