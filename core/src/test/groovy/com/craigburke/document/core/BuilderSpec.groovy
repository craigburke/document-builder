package com.craigburke.document.core

import spock.lang.Shared
import spock.lang.Specification

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

    def "use typographic units"() {
        when:
        builder.create {
            document(margin: [top: 2.inches, bottom: 1.inch]) {
                paragraph(font: [size: 12.pt]) {
                    text 'Foo'
                }
                table(border: [size: 2.px]) {
                    row {
                        column 'Bar'
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
                        column('Column1')
                        column('Column2')
                        column('Column3')
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

    def "columns can be counted correctly"() {
        def externalMethod = { }
        String externalProperty

        when:
        def result = builder.create {
            document {
                table {
                    externalMethod()
                    externalProperty = 'TEST'

                    row {
                        column 'Column1-1'
                        column 'Column1-2'
                    }
                    row {
                        column 'Column2-1'
                        column 'Column2-2'
                        column 'Column2-3'
                    }

                }

            }
        }

        Table table = result.document.children[0]

        then:
        table.columnCount == 3
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
                        column('FOO')
                        column {
                            text 'BAR'
                        }
                    }
                }
            }
        }

        Document document = result.document
        Table table = document.children[0]

        Row row = table.children[0]

        Column column1 = row.children[0]
        Column column2 = row.children[1]

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
                        column 'FOOBAR'
                        column 'BLAH'
                    }
                }
            }
        }

        Table table = result.document.children[0]
        Column column1 = table.children[0].children[0]
        Column column2 = table.children[0].children[1]

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
                        column('Override')
                    }
                }

                table {
                    row {
                        column('Default font')
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
                        column {
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
                table(backgroundColor: backgroundColors[0]) {
                    row {
                        column '1.1'
                        column '1.2'
                    }
                }

                table {
                    row(backgroundColor: backgroundColors[1]) {
                        column '2.1'
                        column '2.2'
                    }
                }

                table {
                    row {
                        column '3-1', backgroundColor: backgroundColors[2]
                        column '3-2'
                    }
                }
            }
        }.document

        Table table1 = result.children[0]
        Table table2 = result.children[1]
        Table table3 = result.children[2]

        then:
        table1.backgroundColor.hex == backgroundColors[0] - '#'
        table1.children[0].backgroundColor.hex == backgroundColors[0] - '#'
        table1.children[0].children.each { Column column ->
            assert column.backgroundColor.hex == backgroundColors[0] - '#'
        }

        and:
        table2.backgroundColor == null
        table2.children[0].backgroundColor.hex == backgroundColors[1] - '#'
        table2.children[0].children.each { Column column ->
            assert column.backgroundColor.hex == backgroundColors[1] - '#'
        }

        and:
        table3.backgroundColor == null
        table3.children[0].backgroundColor == null
        table3.children[0].children[0].backgroundColor.hex == backgroundColors[2] - '#'
        table3.children[0].children[1].backgroundColor == null
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

    def "table within a table"() {
        when:
        Document result = builder.create {
            document {
                table {
                    row {
                        column 'OUTER TABLE'
                        column {
                            table {
                                row {
                                    column 'INNER TABLE'
                                }
                            }
                        }
                    }
                }
            }
        }.document

        Table outerTable = result.children[0]
        Table innerTable = outerTable.children[0].children[1].children[0]

        then:
        outerTable.children[0].children[0].children[0].text == 'OUTER TABLE'

        and:
        innerTable.children[0].children[0].children[0].text == 'INNER TABLE'
    }

    def "widths are set correct with table within a table"() {
        when:
        Document result = builder.create {
            document {
                table(width: 450, columns: [200, 250]) {
                    row {
                        column {
                            table(width: 400, padding: 0) {
                                row {
                                    column 'INNER TABLE'
                                }
                            }
                        }
                        column()
                    }
                }
            }
        }.document

        Table outerTable = result.children[0]
        Table innerTable = outerTable.children[0].children[0].children[0]

        then:
        outerTable.width == 450

        and:
        outerTable.children[0].children[0].width == 200

        and:
        innerTable.width == 180
    }

    def "widths are set correctly with table that uses colspans"() {
        when:
        Document result = builder.create {
            document {
                table(width: 450, columns: [200, 100, 150]) {
                    row {
                        column(colspan:2)
                        column()
                    }
                }
            }
        }.document

        Table table = result.children[0]
        Row row = table.children[0]

        then:
        table.width == 450

        and:
        row.children[0].width == 300

        and:
        row.children[1].width == 150
    }

    def "columns are repeated when rowspan is specified"() {
        when:
        Document result = builder.create {
            document {
                table {
                    row {
                        column(rowspan:3)
                        column()
                        column()
                    }
                    row {
                        column()
                        column()
                    }
                    row {
                        column()
                        column()
                    }
                    row {
                        column()
                        column()
                        column()
                    }
                }
            }
        }.document

        Table table = result.children[0]
        Row row1 = table.children[0]
        Row row2 = table.children[1]
        Row row3 = table.children[2]
        Row row4 = table.children[3]

        then:
        row1.children.size() == 3

        and:
        row2.children.size() == 3
        row1.children[0] == row2.children[0]

        and:
        row3.children.size() == 3
        row1.children[0] == row3.children[0]

        and:
        row4.children.size() == 3
        row1.children[0] != row4.children[0]
    }

}
