package com.craigburke.document.core

import groovy.transform.AutoClone

/**
 * Color attributes for borders and text
 * @author Craig Burke
 */
@AutoClone
class Color {
    String hex = '000000'
    def rgb = [0, 0, 0]

    void setColor(String value) {
        if (value.startsWith('#')) {
            String hexString = value[1..-1]
            this.hex = hexString
            this.rgb = (hexString =~ /.{2}/).collect { Integer.parseInt(it, 16) }
        }
    }

    private setHex(String value) {
        throw new UnsupportedOperationException("Cannot directly set hex to ${value}, use the color property")
    }

    private setRgb(value) {
        throw new UnsupportedOperationException("Cannot directly set rgb to ${value}, use the color property")
    }

}
