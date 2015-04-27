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
        paragraphElement.parsedStart == 0

        and:
        paragraphElement.parsedLinesCount == 3
        
        and:
        paragraphElement.fullyParsed
    }

    def "Can parse a single line"() {
        when:
        paragraphElement.with {
            parse(defaultLineHeight)
        }

        then:
        paragraphElement.parsedStart == 0

        and:
        paragraphElement.parsedLinesCount == 1

        when:
        paragraphElement.with {
            render(0)
            parse(defaultLineHeight)
        }

        then:
        paragraphElement.parsedStart == 1

        and:
        paragraphElement.parsedLinesCount == 1

        when:
        paragraphElement.with {
            render(0)
            parse(defaultLineHeight)
            render(0)
        }

        then:
        paragraphElement.parsedStart == 2

        and:
        paragraphElement.parsedLinesCount == 1

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

            assert paragraphElement.parsedStart == 0
            assert paragraphElement.parsedLinesCount == lineCount
            assert paragraphElement.parsedHeight == parseHeight
        }

        then:
        notThrown(Exception)
    }

}
