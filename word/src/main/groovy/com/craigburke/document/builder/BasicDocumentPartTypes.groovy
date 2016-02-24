package com.craigburke.document.builder

/**
 * Enum for the various types of document parts a relationship can exist in
 * @author Craig Burke
 */
enum BasicDocumentPartTypes implements DocumentPartType {
    ROOT('root', null, null),

    DOCUMENT('document',
            'document.xml',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument'
    ),

    CORE_PROPERTIES('core',
            'docProps/core.xml',
            'application/vnd.openxmlformats-package.core-properties+xml',
            'http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties'
    ),

    APP_PROPERTIES('app',
            'docProps/app.xml',
            'application/vnd.openxmlformats-officedocument.extended-properties+xml',
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties'
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

    BasicDocumentPartTypes(String value, String fileName, String contentType, String relationshipType = null) {
        this.value = value
        this.fileName = fileName
        this.contentType = contentType
        this.relationshipType = relationshipType
    }
}
