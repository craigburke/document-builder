package com.craigburke.document.core

/**
 * Category that allows for the use of typographic units like inches
 * @author Craig Burke
 */
@Category(Number)
class UnitCategory {
    BigDecimal getInches() { this * UnitUtil.DPI }
    BigDecimal getInch() { this * UnitUtil.DPI }
    BigDecimal getPt() { this }
    BigDecimal getPx() { this }
}
