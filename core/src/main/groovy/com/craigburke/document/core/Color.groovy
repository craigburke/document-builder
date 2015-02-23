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

            setHex(hexString)
        }
    }

    void setHex(String value) {
        this.hex = value
        this.rgb = (value =~ /.{2}/).collect { Integer.parseInt(it, 16) }
    }

    void setRgb(value) {
        this.rgb = value
        this.hex = value.collect { Integer.toHexString(it) }.join('')
    }

}
