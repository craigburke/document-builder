package com.craigburke.document.core

import spock.lang.Specification
import spock.lang.Unroll

/**
 * UnitUtil tests
 * @author Craig Burke
 */
class UnitUtilSpec extends Specification {

    @Unroll
    def "covert point to inch"() {
        expect:
        UnitUtil.pointToInch(point) == result

        where:
        point | result
        0     | 0
        72    | 1
        108   | 1.5
    }

    @Unroll
    def "convert inch to point"() {
        expect:
        UnitUtil.inchToPoint(inch) == result

        where:
        inch | result
        0     | 0
        1     | 72
        1.5   | 108
    }

    @Unroll
    def "covert point to twip"() {
        expect:
        UnitUtil.pointToTwip(point) == result

        where:
        point | result
        0     | 0
        1     | 20
        1.5   | 30
        2     | 40
    }

    @Unroll
    def "convert twip to point"() {
        expect:
        UnitUtil.twipToPoint(twip) == result

        where:
        twip  | result
        0     | 0
        20    | 1
        30    | 1.5
        40    | 2
    }

    @Unroll
    def "covert point to pica"() {
        expect:
        UnitUtil.pointToPica(point) == result

        where:
        point | result
        0     | 0
        1     | 6
        1.5   | 9
        2     | 12
    }

    @Unroll
    def "convert pica to point"() {
        expect:
        UnitUtil.picaToPoint(pica) == result

        where:
        pica  | result
        0     | 0
        6     | 1
        9     | 1.5
        12    | 2
    }

    @Unroll
    def "covert point to eight point"() {
        expect:
        UnitUtil.pointToEigthPoint(point) == result

        where:
        point | result
        0     | 0
        1     | 8
        1.5   | 12
        2     | 16
    }

    @Unroll
    def "convert eight point to point"() {
        expect:
        UnitUtil.eightPointToPoint(eightPoint) == result

        where:
        eightPoint  | result
        0           | 0
        8           | 1
        12          | 1.5
        16          | 2
    }

}
