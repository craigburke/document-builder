package com.craigburke.document.core

/**
 * Standard paper size utility. Returned dimensions are inches.
 */
class PaperSize {

    static final Dimension A1 = new Dimension(23.4, 33.1)
    static final Dimension A2 = new Dimension(16.5, 23.4)
    static final Dimension A3 = new Dimension(11.7, 16.5)
    static final Dimension A4 = new Dimension(8.27, 11.7)
    static final Dimension A5 = new Dimension(5.83, 8.27)
    static final Dimension A6 = new Dimension(4.13, 5.83)
    static final Dimension LETTER = new Dimension(8.5, 11)
    static final Dimension LEGAL = new Dimension(8.5, 14)

    static Dimension get(String name) {
        switch (name.toLowerCase()) {
            case 'a1':
                return A1
            case 'a2':
                return A2
            case 'a3':
                return A3
            case 'a4':
                return A4
            case 'a5':
                return A5
            case 'a6':
                return A6
            case 'letter':
                return LETTER
            case 'legal':
                return LEGAL
            default:
                throw new IllegalArgumentException("invalid paper size: $name")
        }
    }
}
