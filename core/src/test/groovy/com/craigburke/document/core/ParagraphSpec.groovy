package com.craigburke.document.core

import spock.lang.Shared
import spock.lang.Specification

/**
 * Paragraph tests
 * @author Craig Burke
 */
class ParagraphSpec extends Specification {

    @Shared Paragraph paragraph
    static final int DEFAULT_FONT_SIZE = 12

    def setup() {
        paragraph = new Paragraph()
        paragraph.children << new Text(font:[size:DEFAULT_FONT_SIZE])
    }

    def "no leading or text for empty paragraph"() {
        Paragraph emptyParagraph = new Paragraph(font:[size:10])

        expect:
        emptyParagraph.lineHeight == 0
        emptyParagraph.text == ''
    }

    def "leading default"() {
        expect:
        paragraph.lineHeight == DEFAULT_FONT_SIZE * paragraph.leadingMultiplier
    }

    def "leading override"() {
        paragraph.leading = 20

        expect:
        paragraph.leading == 20
        paragraph.lineHeight == 20
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
