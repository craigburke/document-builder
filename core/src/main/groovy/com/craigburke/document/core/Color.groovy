package com.craigburke.document.core

class Color {
    private String _hex
    private def _rgb

    Color(String color) {
        set(color)
    }

    void set(String color) {
        if (color.startsWith('#')) {
            this._hex = color[1..-1]
            this._rgb = (_hex =~ /.{2}/).collect { Integer.parseInt(it, 16) }
        }
    }

    String getHex() {
        _hex
    }

    def getRGB() {
        _rgb
    }


}
