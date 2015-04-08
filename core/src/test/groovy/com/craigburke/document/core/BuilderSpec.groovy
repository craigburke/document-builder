package com.craigburke.document.core

import spock.lang.Shared
import spock.lang.Specification

/**
 * Builder core tests
 * @author Craig Burke
 */
class BuilderSpec extends Specification {

    @Shared TestBuilder builder
    @Shared byte[] imageData = getClass().classLoader.getResource('test/images/cheeseburger.jpg')?.bytes

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
            document(margin:[top:2.inches, bottom:1.inch]) {
                paragraph(font:[size:12.pt]) {
                    text 'Foo'
                }
                table(border:[size:2.px]) {
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
                addFont('/open-sans.ttf', name:'Open Sans', bold:true)
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

    def 'Table events are all called after they finish'() {
        def onTableComplete = Mock(Closure)
        builder.onTableComplete = { Table table -> onTableComplete(table) }

        def onRowComplete = Mock(Closure)
        builder.onRowComplete = { Row row, Table table -> onRowComplete(row, table) }

        def onCellComplete = Mock(Closure)
        builder.onCellComplete = { Cell cell, Row row -> onCellComplete(cell, row) }

        when:
        builder.create {
            document {
                table {
                    row {
                        cell('Cell1')
                        cell('Cell2')
                        cell('Cell3')
                    }

                }
            }
        }

        then:
        1 * onTableComplete.call(_ as Table)
        1 * onRowComplete.call(_ as Row, _ as Table)
        3 * onCellComplete.call(_ as Cell, _ as Row)
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
                        cell 'CELL1-1'
                        cell 'CELL1-2'
                    }
                    row {
                        cell 'CELL2-1'
                        cell 'CELL2-2'
                        cell 'CELL2-3'
                    }

                }

            }
        }

        Table table = result.document.children[0]

        then:
        table.columns == 3
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
                paragraph 'left', align:Align.LEFT
                paragraph 'center', align:Align.CENTER
                paragraph 'right', align:Align.RIGHT
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

        Cell cell1 = row.children[0]
        Cell cell2 = row.children[1]

        TextBlock paragraph1 = cell1.children[0]
        TextBlock paragraph2 = cell2.children[0]

        Text text1 = paragraph1.children[0]
        Text text2 = paragraph2.children[0]

        then:
        table.parent == document

        and:
        table.children == [row]
        row.parent == table
        row.children == [cell1, cell2]

        and:
        cell1.parent == row
        cell1.children == [paragraph1]
        paragraph1.parent == cell1

        and:
        cell2.parent == row
        cell2.children == [paragraph2]
        paragraph2.parent == cell2

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
                table(width:250, padding:0, border:[size:0]) {
                    row {
                        cell('FOOBAR', width:100)
                        cell('BLAH')
                    }
                }
            }
        }

        Table table = result.document.children[0]
        Cell cell1 = table.children[0].children[0]
        Cell cell2 = table.children[0].children[1]

        then:
        table.width == 250

        and:
        cell1.width == 100

        and:
        cell2.width == 150
    }

    def "override or inherit font settings"() {
        when:
        def result = builder.create {
            document(font:[family:'Helvetica', color:'#121212']) {

                paragraph(font:[family:'Courier', color:'#333333']) {
                    text 'Paragraph override'
                }
                paragraph 'Inherit doc font'

                paragraph {
                    text 'Text override', font:[family:'Times-Roman', color:'#FFFFFF']
                }

                table(font:[family:'Courier', color:'#111111']) {
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
                            image(data:imageData, width:500.px, height:431.px)
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
                table(backgroundColor:backgroundColors[0]) {
                    row {
                        cell '1.1'
                        cell '1.2'
                    }
                }

                table {
                    row(backgroundColor:backgroundColors[1]) {
                        cell '2.1'
                        cell '2.2'
                    }
                }

                table {
                    row {
                        cell '3-1', backgroundColor:backgroundColors[2]
                        cell '3-2'
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
        table1.children[0].children.each { Cell cell ->
            assert cell.backgroundColor.hex == backgroundColors[0] - '#'
        }

        and:
        table2.backgroundColor == null
        table2.children[0].backgroundColor.hex == backgroundColors[1] - '#'
        table2.children[0].children.each { Cell cell ->
            assert cell.backgroundColor.hex == backgroundColors[1] - '#'
        }

        and:
        table3.backgroundColor == null
        table3.children[0].backgroundColor == null
        table3.children[0].children[0].backgroundColor.hex == backgroundColors[2] - '#'
        table3.children[0].children[1].backgroundColor == null
    }

}
