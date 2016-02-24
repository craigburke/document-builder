package com.craigburke.document.core

/**
 * Get version of document builder.
 *
 */
class Version {

    private static final String VERSION_PROPERTIES = 'document-builder.properties'

    private Version() {}

    static String getVersion() {
        String version = 'unknown'
        try {
            Properties props = new Properties()
            InputStream is = Version.class.getClassLoader().getResourceAsStream(VERSION_PROPERTIES)
            try {
                props.load(is)
            } finally {
                is.close()
            }
            version = props.getProperty('document-builder.version', version)
        } catch (IOException e) {
            e.printStackTrace()
        }
        version
    }

}
