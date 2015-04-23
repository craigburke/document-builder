package com.craigburke.document.builder

import com.craigburke.document.builder.render.ParagraphElement
import com.craigburke.document.core.Document
import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.Text
import com.craigburke.document.core.TextBlock
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by craig on 4/17/15.
 */
class ParagraphElementSpec extends Specification {

    @Shared
    ParagraphElement paragraphElement

    def setup() {
        TextBlock paragraph = new TextBlock(margin: TextBlock.defaultMargin, font: [size: 12, family: 'Helvetica'])
        3.times {
            if (it != 1) {
                paragraph.children << new LineBreak()
            }
            paragraph.children << new Text(value: "Line${it}", font: paragraph.font.clone())
        }

        Document document = new Document(margin: Document.defaultMargin)
        PdfDocument pdfDocument = new PdfDocument(document)
        paragraphElement = new ParagraphElement(paragraph, pdfDocument, 0, 200)
    }

    def "Can parse all lines"() {
        when:
        paragraphElement.parse(100)

        then:
        paragraphElement.lines.size() == 3

        and:
        paragraphElement.parsedStart == 0

        and:
        paragraphElement.parsedLinesCount == 3
    }

    def "Can parse a single line"() {
        when:
        paragraphElement.with {
            parse(20)
        }

        then:
        paragraphElement.parsedStart == 0

        and:
        paragraphElement.parsedLinesCount == 1

        when:
        paragraphElement.with {
            render(0)
            parse(20)
        }

        then:
        paragraphElement.parsedStart == 1

        and:
        paragraphElement.parsedLinesCount == 1

        when:
        paragraphElement.with {
            render(0)
            parse(20)
            render(0)
        }

        then:
        paragraphElement.parsedStart == 2

        and:
        paragraphElement.parsedLinesCount == 1

        and:
        paragraphElement.fullyParsed == true

        and:
        paragraphElement.fullyRendered == true
    }

}
