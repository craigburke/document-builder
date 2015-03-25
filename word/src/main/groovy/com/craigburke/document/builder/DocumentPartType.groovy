package com.craigburke.document.builder

/**
 * Enum for the various types of document parts a relationship can exist in
 * @author Craig Burke
 */
enum DocumentPartType {
    ROOT('root', null, null),
    DOCUMENT('document',
            'document.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml'
    ),
    HEADER('header', 'header.xml', 'application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml'),
    FOOTER('footer', 'footer.xml', 'application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml')

    final String value
    final String fileName
    final String contentType

    DocumentPartType(String value, String fileName, String contentType) {
        this.value = value
        this.fileName = fileName
        this.contentType = contentType
    }
}
