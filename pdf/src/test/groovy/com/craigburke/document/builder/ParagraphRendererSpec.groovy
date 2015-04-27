package com.craigburke.document.builder

import com.craigburke.document.builder.render.ParagraphRenderer
import com.craigburke.document.core.TextBlock
import spock.lang.Shared

/**
 * Paragraph element tests
 * @author Craig Burke
 */
class ParagraphRendererSpec extends RendererTestBase {

    @Shared
    ParagraphRenderer paragraphElement

    def setup() {
        TextBlock paragraph = makeParagraph(3)
        paragraphElement = makeParagraphElement(paragraph)
    }

    def "Can parse all lines"() {
        float height = defaultLineHeight * 3f

        when:
        paragraphElement.parse(height)

        then:
        paragraphElement.lines.size() == 3

        and:
        paragraphElement.parseStart == 0

        and:
        paragraphElement.parseEnd == 2

        and:
        paragraphElement.fullyParsed
    }

    def "Can parse a single line"() {
        when:
        paragraphElement.with {
            parse(defaultLineHeight)
        }

        then:
        paragraphElement.parseStart == 0

        and:
        paragraphElement.parseEnd == 0

        when:
        paragraphElement.with {
            render(0)
            parse(defaultLineHeight)
        }

        then:
        paragraphElement.parseStart == 1

        and:
        paragraphElement.parseEnd == 1

        when:
        paragraphElement.with {
            render(0)
            parse(defaultLineHeight)
            render(0)
        }

        then:
        paragraphElement.parseStart == 2

        and:
        paragraphElement.parseEnd == 2

        and:
        paragraphElement.fullyParsed

        and:
        paragraphElement.fullyRendered
    }

    def "can parse multiple times without rendering"() {
        when:
        3.times { i ->
            int lineCount = i + 1
            float parseHeight = defaultLineHeight * lineCount
            paragraphElement.parse(parseHeight)

            assert paragraphElement.parseStart == 0
            assert paragraphElement.parseEnd == lineCount - 1
            assert paragraphElement.parsedHeight == parseHeight
        }

        then:
        notThrown(Exception)
    }

}
