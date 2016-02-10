package com.craigburke.document.core

import com.craigburke.document.core.builder.DocumentBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Builder core tests
 * @author Craig Burke
 */
class BuilderSpec extends Specification {

    @Shared
    TestBuilder builder
    @Shared
    byte[] imageData = getClass().classLoader.getResource('test/images/cheeseburger.jpg')?.bytes

    def setup() {
        OutputStream out = new ByteArrayOutputStream()
        builder = new TestBuilder(out)
    }

    def "create empty document"() {
        when:
        def result = builder.create {
            document()
        }

        then:
        result.document != null

        and:
        result.document.getClass() == Document
    }

    def "use file in builder constructor"() {
        File testFile = new File('test')

        when:
        def fileBuilder = new TestBuilder(testFile)

        then:
        fileBuilder.out != null

        and:
        fileBuilder.out.getClass() == FileOutputStream

        cleanup:
        testFile?.delete()
    }

    def "create default letter size document"() {
        when:
        def result = builder.create {
            document(margin: [top: 2.cm, bottom: 1.cm]) {
                paragraph(align: 'center', font: [size: 24.pt]) {
                    text 'ISO 216'
                }
            }
        }

        then:
        result.document.width == 612 // 8.5 inch * 72 DPI
        result.document.height == 792 // 11 inch * 72 DPI
    }

    def "create A4 document"() {
        when:
        def result = builder.create {
            document(size: 'A4', margin: [top: 2.cm, bottom: 1.cm]) {
                paragraph(align: 'center', font: [size: 24.pt]) {
                    text 'ISO 216'
                }
            }
        }

        then:
        result.document.width == 595 // 8.27 inch * 72 DPI
        result.document.height == 842 // 11.7 inch * 72 DPI
    }

    def "use landscape orientation"() {
        when:
        def result = builder.create {
            document(size: 'A4', orientation: 'landscape', margin: [top: 2.cm, bottom: 1.cm]) {
                paragraph(align: 'center', font: [size: 24.pt]) {
                    text 'Landscape'
                }
            }
        }

        then:
        result.document.width == 842 // 11.7 inch * 72 DPI
        result.document.height == 595 // 8.27 inch * 72 DPI
    }

    def "use typographic units"() {
        when:
        builder.create {
            document(margin: [top: 2.inches, bottom: 1.inch]) {
                paragraph(font: [size: 12.pt]) {
                    text 'Foo'
                }
                table(border: [size: 2.px]) {
                    row {
                        cell 'Bar'
                    }
                }
            }
        }

        then:
        notThrown(Exception)
    }

    def "load embedded font"() {
        when:
        def result = builder.create {
            document {
                addFont('/open-sans.ttf', name: 'Open Sans', bold: true)
            }
        }

        def embeddedFont = result.document.embeddedFonts.first()

        then:
        embeddedFont.name == 'Open Sans'
    }

    def 'onTextBlockComplete is called after a paragraph finishes'() {
        def onTextBlockComplete = Mock(Closure)
        builder.onTextBlockComplete = { TextBlock paragraph -> onTextBlockComplete(paragraph) }

        when:
        builder.create {
            document {
                paragraph 'FOO BAR!'
            }
        }

        then:
        1 * onTextBlockComplete.call(_ as TextBlock)
    }

    def 'onTableComple is called after table finishes'() {
        def onTableComplete = Mock(Closure)
        builder.onTableComplete = { Table table -> onTableComplete(table) }

        when:
        builder.create {
            document {
                table {
                    row {
                        cell('Column1')
                        cell('Column2')
                        cell('Column3')
                    }

                }
            }
        }

        then:
        1 * onTableComplete.call(_ as Table)
    }

    def "Text element shouldn't have children"() {
        when:
        builder.create {
            document {
                paragraph {
                    text {
                        text 'FOOBAR!'
                    }
                }

            }
        }

        then:
        thrown(Exception)
    }

    def "LineBreak element shouldn't have children"() {
        when:
        builder.create {
            document {
                paragraph {
                    lineBreak {
                        lineBreak()
                    }
                }

            }
        }

        then:
        thrown(Exception)
    }

    def "Image element shouldn't have children"() {
        when:
        builder.create {
            document {
                paragraph {
                    image {
                        image()
                    }
                }

            }
        }

        then:
        thrown(Exception)
    }

    def "create a simple paragraph"() {
        when:
        def result = builder.create {
            document {
                paragraph 'FOO BAR!'
            }
        }

        TextBlock paragraph = result.document.children[0]

        then:
        paragraph.text == 'FOO BAR!'
    }

    def "create paragraphs with aligned text"() {
        when:
        def result = builder.create {
            document {
                paragraph 'default'
                paragraph 'left', align: Align.LEFT
                paragraph 'center', align: Align.CENTER
                paragraph 'right', align: Align.RIGHT
            }
        }

        TextBlock paragraph1 = result.document.children[0]
        TextBlock paragraph2 = result.document.children[1]
        TextBlock paragraph3 = result.document.children[2]
        TextBlock paragraph4 = result.document.children[3]

        then:
        paragraph1.align == Align.LEFT

        and:
        paragraph2.align == Align.LEFT

        and:
        paragraph3.align == Align.CENTER

        and:
        paragraph4.align == Align.RIGHT
    }

    def "create paragraph with correct hierarchy"() {
        when:
        def result = builder.create {
            document {
                paragraph {
                    text 'FOO'
                    text 'BAR'
                }
            }
        }

        Document document = result.document
        TextBlock paragraph = document.children[0]
        Text text1 = paragraph.children[0]
        Text text2 = paragraph.children[1]

        then:
        document.children == [paragraph]

        and:
        paragraph.text == 'FOOBAR'
        paragraph.children == [text1, text2]

        and:
        paragraph.parent == document

        and:
        text1.parent == paragraph

        and:
        text2.parent == paragraph
    }

    def "create table with the correct heirarchy"() {
        when:
        def result = builder.create {
            document {
                table {
                    row {
                        cell('FOO')
                        cell {
                            text 'BAR'
                        }
                    }
                }
            }
        }

        Document document = result.document
        Table table = document.children[0]

        Row row = table.children[0]

        Cell column1 = row.children[0]
        Cell column2 = row.children[1]

        TextBlock paragraph1 = column1.children[0]
        TextBlock paragraph2 = column2.children[0]

        Text text1 = paragraph1.children[0]
        Text text2 = paragraph2.children[0]

        then:
        table.parent == document

        and:
        table.children == [row]
        row.parent == table
        row.children == [column1, column2]

        and:
        column1.parent == row
        column1.children == [paragraph1]
        paragraph1.parent == column1

        and:
        column2.parent == row
        column2.children == [paragraph2]
        paragraph2.parent == column2

        and:
        text1.value == 'FOO'
        text1.parent == paragraph1
        paragraph1.children == [text1]

        and:
        text2.value == 'BAR'
        text2.parent == paragraph2
        paragraph2.children == [text2]
    }

    def "column widths are calculated"() {
        when:
        def result = builder.create {
            document {
                table(width: 250, padding: 0, border: [size: 0]) {
                    row {
                        cell 'FOOBAR'
                        cell 'BLAH'
                    }
                }
            }
        }

        Table table = result.document.children[0]
        Cell column1 = table.children[0].children[0]
        Cell column2 = table.children[0].children[1]

        then:
        table.width == 250

        and:
        column1.width == 125

        and:
        column2.width == 125
    }

    def "override or inherit font settings"() {
        when:
        def result = builder.create {
            document(font: [family: 'Helvetica', color: '#121212']) {

                paragraph(font: [family: 'Courier', color: '#333333']) {
                    text 'Paragraph override'
                }
                paragraph 'Inherit doc font'

                paragraph {
                    text 'Text override', font: [family: 'Times-Roman', color: '#FFFFFF']
                }

                table(font: [family: 'Courier', color: '#111111']) {
                    row {
                        cell('Override')
                    }
                }

                table {
                    row {
                        cell('Default font')
                    }
                }

            }
        }

        Document document = result.document

        def paragraph1 = document.children[0].children[0]
        def paragraph2 = document.children[1].children[0]
        def paragraph3 = document.children[2].children[0]

        def table1 = document.children[3].children[0].children[0].children[0]
        def table2 = document.children[4].children[0].children[0].children[0]

        then:
        paragraph1.font.family == 'Courier'

        and:
        paragraph2.font.family == 'Helvetica'

        and:
        paragraph3.font.family == 'Times-Roman'

        and:
        table1.font.family == 'Courier'

        and:
        table2.font.family == 'Helvetica'
    }

    def "create a table with that contains an image and text"() {
        when:
        builder.create {
            document {
                table {
                    row {
                        cell {
                            image(data: imageData, width: 500.px, height: 431.px)
                            lineBreak()
                            text 'A cheeseburger'
                        }
                    }

                }
            }
        }

        then:
        notThrown(Exception)
    }

    def "background color cascades"() {
        given:
        String[] backgroundColors = ['#000000', '#111111', '#333333']

        when:
        Document result = builder.create {
            document {
                table(background: backgroundColors[0]) {
                    row {
                        cell '1.1'
                        cell '1.2'
                    }
                }

                table {
                    row(background: backgroundColors[1]) {
                        cell '2.1'
                        cell '2.2'
                    }
                }

                table {
                    row {
                        cell '3-1', background: backgroundColors[2]
                        cell '3-2'
                    }
                }
            }
        }.document

        Table table1 = result.children[0]
        Table table2 = result.children[1]
        Table table3 = result.children[2]

        then:
        table1.background.hex == backgroundColors[0] - '#'
        table1.children[0].background.hex == backgroundColors[0] - '#'
        table1.children[0].children.each { Cell column ->
            assert column.background.hex == backgroundColors[0] - '#'
        }

        and:
        table2.background == null
        table2.children[0].background.hex == backgroundColors[1] - '#'
        table2.children[0].children.each { Cell column ->
            assert column.background.hex == backgroundColors[1] - '#'
        }

        and:
        table3.background == null
        table3.children[0].background == null
        table3.children[0].children[0].background.hex == backgroundColors[2] - '#'
        table3.children[0].children[1].background == null
    }

    def "set link on linkable nodes"() {
        String url = 'http://www.craigburke.com'

        when:
        Document result = builder.create {
            document {
                heading1 'HEADING1', url: url
                heading2 'HEADING2'

                paragraph 'Paragraph1', url: url
                paragraph {
                    text 'Check this out: '
                    text 'Click on me', url: url
                }
            }
        }.document

        Heading heading1 = result.children[0]
        Heading heading2 = result.children[1]
        TextBlock paragraph1 = result.children[2]
        TextBlock paragraph2 = result.children[3]

        then:
        heading1.url == url

        and:
        heading2.url == null

        and:
        paragraph1.url == url
        paragraph1.children[0].url == url

        and:
        paragraph2.url == null
        paragraph2.children[0].url == null
        paragraph2.children[1].url == url
    }

    @Unroll('Template keys calculated for #description')
    def "template keys are calculated"() {
        expect:
        DocumentBuilder.getTemplateKeys(node, nodeKey) == expectedKeys.toArray()

        where:
        node                                   | nodeKey     || expectedKeys
        new TextBlock()                        | 'paragraph' || ['paragraph']
        new TextBlock(style: 'foo')            | 'paragraph' || ['paragraph', 'paragraph.foo']

        new Text()                             | 'text'      || ['text']
        new Text(style: 'bar')                 | 'text'      || ['text', 'text.bar']

        new Table()                            | 'table'     || ['table']
        new Table(style: 'foo')                | 'table'     || ['table', 'table.foo']
        new Row()                              | 'row'       || ['row']
        new Row(style: 'foo')                  | 'row'       || ['row', 'row.foo']
        new Cell()                             | 'cell'      || ['cell']
        new Cell(style: 'foo')                 | 'cell'      || ['cell', 'cell.foo']

        new Heading(level: 1)                  | 'heading'   || ['heading', 'heading1']
        new Heading(level: 1, style: 'foobar') | 'heading'   || ['heading', 'heading1', 'heading.foobar', 'heading1.foobar']
        new Heading(level: 2)                  | 'heading'   || ['heading', 'heading2']
        new Heading(level: 2, style: 'foobar') | 'heading'   || ['heading', 'heading2', 'heading.foobar', 'heading2.foobar']
        new Heading(level: 3)                  | 'heading'   || ['heading', 'heading3']
        new Heading(level: 3, style: 'foobar') | 'heading'   || ['heading', 'heading3', 'heading.foobar', 'heading3.foobar']
        new Heading(level: 4)                  | 'heading'   || ['heading', 'heading4']
        new Heading(level: 4, style: 'foobar') | 'heading'   || ['heading', 'heading4', 'heading.foobar', 'heading4.foobar']
        new Heading(level: 5)                  | 'heading'   || ['heading', 'heading5']
        new Heading(level: 5, style: 'foobar') | 'heading'   || ['heading', 'heading5', 'heading.foobar', 'heading5.foobar']
        new Heading(level: 6)                  | 'heading'   || ['heading', 'heading6']
        new Heading(level: 6, style: 'foobar') | 'heading'   || ['heading', 'heading6', 'heading.foobar', 'heading6.foobar']

        description = "${nodeKey}${node.style ? ".${node.style}" : ''}"
    }

}
