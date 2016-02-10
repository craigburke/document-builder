package com.craigburke.document.core

/**
 * Utility class for converting typographic units
 * @author Craig Burke
 */
class UnitUtil {
    static final BigDecimal POINTS_PER_INCH = 72
    static final BigDecimal CENTIMETER_PER_INCH = 2.54
    static final BigDecimal POINTS_PER_CENTIMETER = POINTS_PER_INCH / CENTIMETER_PER_INCH
    static final BigDecimal PICA_POINTS = 6
    static final BigDecimal TWIP_POINTS = 20
    static final BigDecimal EIGTH_POINTS = 8
    static final BigDecimal HALF_POINTS = 2
    static final BigDecimal EMU_POINTS = 12700

    static BigDecimal inchToPoint(BigDecimal inch) {
        inch * POINTS_PER_INCH
    }

    static BigDecimal pointToInch(BigDecimal point) {
        point / POINTS_PER_INCH
    }

    static BigDecimal cmToPoint(BigDecimal cm) {
        cm * POINTS_PER_CENTIMETER
    }

    static BigDecimal pointToCm(BigDecimal point) {
        point / POINTS_PER_CENTIMETER
    }

    static BigDecimal pointToPica(BigDecimal point) {
        point * PICA_POINTS
    }

    static BigDecimal picaToPoint(BigDecimal pica) {
        pica / PICA_POINTS
    }

    static BigDecimal pointToEigthPoint(BigDecimal point) {
        point * EIGTH_POINTS
    }

    static BigDecimal eightPointToPoint(BigDecimal eigthPoint) {
        eigthPoint / EIGTH_POINTS
    }

    static BigDecimal pointToHalfPoint(BigDecimal point) {
        point * HALF_POINTS
    }

    static BigDecimal halfPointToPoint(BigDecimal halfPoint) {
        halfPoint / HALF_POINTS
    }

    static BigDecimal pointToTwip(BigDecimal point) {
        point * TWIP_POINTS
    }

    static BigDecimal twipToPoint(BigDecimal twip) {
        twip / TWIP_POINTS
    }

    static BigDecimal pointToEmu(BigDecimal point) {
        point * EMU_POINTS
    }

    static BigDecimal emuToPoint(BigDecimal emu) {
        emu / EMU_POINTS
    }

    static BigDecimal inchToCm(BigDecimal inch) {
        inch * CENTIMETER_PER_INCH
    }

    static BigDecimal cmToInch(BigDecimal inch) {
        inch / CENTIMETER_PER_INCH
    }
}
