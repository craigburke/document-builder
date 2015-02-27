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
        emptyParagraph.textHeight == 0
        emptyParagraph.text == ''
    }

    def "leading default"() {
        expect:
        paragraph.textHeight == Math.ceil(DEFAULT_FONT_SIZE * paragraph.lineHeightMultiplier)
    }

    def "leading override"() {
        paragraph.lineHeight = 20

        expect:
        paragraph.lineHeight == 20
        paragraph.textHeight == 20
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
