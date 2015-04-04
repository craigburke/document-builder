package com.craigburke.document.core

import spock.lang.Shared
import spock.lang.Specification

/**
 * Paragraph tests
 * @author Craig Burke
 */
class ParagraphSpec extends Specification {

    @Shared TextBlock paragraph
    static final int DEFAULT_FONT_SIZE = 12

    def setup() {
        paragraph = new TextBlock()
        paragraph.children << new Text(font:[size:DEFAULT_FONT_SIZE])
    }

    def "text combines text values"() {
        paragraph.children = [
            new Text(value:'FOO'),
            new Text(value:'BAR'),
            new LineBreak(),
            new Text(value:'123')
        ]

        expect:
        paragraph.text == 'FOOBAR123'
    }

}
