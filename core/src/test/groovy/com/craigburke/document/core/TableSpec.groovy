package com.craigburke.document.core

import spock.lang.Shared
import spock.lang.Specification

/**
 * Table tests
 * @author Craig Burke
 */
class TableSpec extends Specification {

    @Shared
    TestBuilder builder

    def setup() {
        OutputStream out = new ByteArrayOutputStream()
        builder = new TestBuilder(out)
    }

    def "table within a table"() {
        when:
        Document result = builder.create {
            document {
                table {
                    row {
                        cell 'OUTER TABLE'
                        cell {
                            table {
                                row {
                                    cell 'INNER TABLE'
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
                table(width: 450, border:[size:0], columns:[200, 250]) {
                    row {
                        cell {
                            table(width: 400, padding: 0) {
                                row {
                                    cell 'INNER TABLE'
                                }
                            }
                        }
                        cell()
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
                table(width: 450, border:[size:0], columns: [200, 100, 150]) {
                    row {
                        cell(colspan:2)
                        cell()
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
                        cell(rowspan:3)
                        cell()
                        cell()
                    }
                    row {
                        cell()
                        cell()
                    }
                    row {
                        cell()
                        cell()
                    }
                    row {
                        cell()
                        cell()
                        cell()
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

    def "column widths are set correctly when rowspan is set"() {
        when:
        Document result = builder.create {
            document {
                table(width: 400.px, columns: [100.px, 300.px], padding: 0, border: [size: 0]) {
                    row {
                        cell(rowspan:3) {
                            text 'COL1-1'
                        }
                        cell('COL1-2')
                    }
                    row {
                        cell('COL2-1')
                    }
                    row {
                        cell('COL3-1')
                    }
                    row {
                        cell('COL4-1')
                        cell('COL4-2')
                    }
                }
            }
        }.document

        Table table = result.children[0]

        then:
        table.children.each {
            assert it.children[0].width == 100
            assert it.children[1].width == 300
        }
    }

}
