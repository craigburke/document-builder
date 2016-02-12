package com.craigburke.document.core

/**
 * An individual cell for the Table node
 * @author Craig Burke
 */
class Cell extends BaseNode implements Stylable, Alignable, BackgroundAssignable, LineSpacingAssignable {
    List children = []
    Integer width
    Integer colspan = 1

    Integer rowspan = 1
    Integer rowsSpanned = 0
    BigDecimal rowspanHeight = 0
}
