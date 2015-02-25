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

    def "user typographic units"() {
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

    def 'onParagraphComplete is called after a paragraph finishes'() {
        def onParagraphComplete = Mock(Closure)
        builder.onParagraphComplete = { Paragraph paragraph -> onParagraphComplete(paragraph) }

        when:
        builder.create {
            document {
                paragraph 'FOO BAR!'
            }
        }

        then:
        1 * onParagraphComplete.call(_ as Paragraph)
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

    def 'addTextToParagraph is call after text element is finished'() {
        def addTextToParagraph = Mock(Closure)
        def addedText = []
        builder.addTextToParagraph = { Text text, Paragraph paragraph ->
            addTextToParagraph(text, paragraph)
            addedText << text.value
        }

        when:
        builder.create {
            document {
                paragraph 'FOO'
                paragraph {
                    text 'BLAH'
                }
                table {
                    row {
                        cell('CELL1')
                        cell {
                            text 'CELL2'
                        }
                    }
                }
            }
        }

        then:
        4 * addTextToParagraph.call(_ as Text, _ as Paragraph)

        and:
        addedText == ['FOO', 'BLAH', 'CELL1', 'CELL2']
    }

    def "addTableToDocument is called when table is created"() {
        def addTableToDocument = Mock(Closure)
        builder.addTableToDocument = { Table table, Document document -> addTableToDocument(table, document) }

        when:
        builder.create {
            document {
                table {
                    row {
                        cell 'FOO'
                    }
                }
            }
        }

        then:
        1 * addTableToDocument.call(_ as Table, _ as Document)
    }

    def "appropriate method is called when paragraph is added"() {
        def addParagraphToDocument = Mock(Closure)
        builder.addParagraphToDocument = { Paragraph paragraph, Document document ->
            addParagraphToDocument(paragraph, document)
        }

        def addParagraphToCell = Mock(Closure)
        builder.addParagraphToCell = { Paragraph paragraph, Cell cell -> addParagraphToCell(paragraph, cell) }

        when:
        builder.create {
            document {
               paragraph 'PARAGRAPH1'
               table {
                   row {
                       cell 'CELL1'
                   }
               }
            }
        }

        then:
        1 * addParagraphToDocument.call(_ as Paragraph, _ as Document)

        and:
        1 * addParagraphToCell.call(_ as Paragraph, _ as Cell)
    }

    def "create a simple paragraph"() {
        when:
        def result = builder.create {
            document {
                paragraph 'FOO BAR!'
            }
        }

        Paragraph paragraph = result.document.children[0]

        then:
        paragraph.text == 'FOO BAR!'
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
        Paragraph paragraph = document.children[0]
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

        Row row = table.rows[0]

        Cell cell1 = row.cells[0]
        Cell cell2 = row.cells[1]

        Paragraph paragraph1 = cell1.children[0]
        Paragraph paragraph2 = cell2.children[0]

        Text text1 = paragraph1.children[0]
        Text text2 = paragraph2.children[0]

        then:
        table.parent == document

        and:
        table.rows == [row]
        row.parent == table
        row.cells == [cell1, cell2]

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
        Cell cell1 = table.rows[0].cells[0]
        Cell cell2 = table.rows[0].cells[1]

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

        def table1 = document.children[3].rows[0].cells[0].children[0]
        def table2 = document.children[4].rows[0].cells[0].children[0]

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

}
