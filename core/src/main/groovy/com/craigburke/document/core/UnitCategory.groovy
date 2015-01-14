package com.craigburke.document.core

@Category(Number)
class UnitCategory {
    BigDecimal getInches() { this * 72 }
    BigDecimal getInch() { this * 72 }
    BigDecimal getPt() { this }
    BigDecimal getPx() { this }
}
