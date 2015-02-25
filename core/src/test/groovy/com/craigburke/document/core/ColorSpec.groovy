package com.craigburke.document.core

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Color tests
 * @author Craig Burke
 */
class ColorSpec extends Specification {

    static final BLACK_RGB = [0, 0, 0]

    @Unroll
    def "set color"() {
        Color color = new Color()

        when:
        color.color = rgb

        then:
        color.rgb == rgb
        color.hex == hex - '#'

        where:
        hex          | rgb
        '000000'     | BLACK_RGB
        '#000000'    | BLACK_RGB
    }

    def "shouldn't be able to directly set hex"() {
        Color color = new Color()

        when:
        color.hex = BLACK_RGB

        then:
        thrown(UnsupportedOperationException)
    }

    def "shouldn't be able to directly set rgb"() {
        Color color = new Color()

        when:
        color.rgb = '000000'

        then:
        thrown(UnsupportedOperationException)
    }

}
