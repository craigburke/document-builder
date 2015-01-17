package com.craigburke.document.core

import groovy.transform.AutoClone

@AutoClone
class Color {
    String hex = "000000"
    def RGB = [0, 0, 0]
    
    void setColor(String value) {
        if (value.startsWith('#')) {
            String hexString = value[1..-1]
            
            setHex(hexString)
        }
    }

    void setHex(String value) {
        this.hex = value
        this.RGB = (value =~ /.{2}/).collect { Integer.parseInt(it, 16) }
    }
    
    void setRGB(value) {
        this.RGB = value
        this.hex = value.collect { Integer.toHexString(it) }.join('')
    }

}
