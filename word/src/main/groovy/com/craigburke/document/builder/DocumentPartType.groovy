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

    HEADER('header',
            'header.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/header'
    ),

    FOOTER('footer',
            'footer.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer'
    ),

    NUMBERING('numbering',
            'nubering.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering'
    ),

    STYLES('styles',
            'styles.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles'
    ),

    SETTINGS('settings',
            'settings.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings'
    ),

    WEB_SETTINGS('webSettings',
            'webSettings.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings'
    ),

    STYLES_WITH_EFFECTS('stylesWithEffects',
            'stylesWithEffects.xml',
            'application/vnd.ms-word.stylesWithEffects+xml',
            'http://schemas.microsoft.com/office/2007/relationships/stylesWithEffects'
    ),

    FONT_TABLE('fontTable',
            'fontTable.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable'
    ),



    final String value
    final String fileName
    final String contentType
    final String relationshipType

    DocumentPartType(String value, String fileName, String contentType, String relationshipType = null) {
        this.value = value
        this.fileName = fileName
        this.contentType = contentType
        this.relationshipType = relationshipType
    }
}
