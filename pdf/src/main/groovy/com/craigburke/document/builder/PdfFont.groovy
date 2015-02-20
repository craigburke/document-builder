package com.craigburke.document.builder

import com.craigburke.document.core.Font
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font

class PdfFont {

    private static def fonts = [
        'Times-Roman':
            [regular: PDType1Font.TIMES_ROMAN, bold: PDType1Font.TIMES_BOLD, italic: PDType1Font.TIMES_ITALIC, boldItalic: PDType1Font.TIMES_BOLD_ITALIC],
        'Helvetica':
            [regular: PDType1Font.HELVETICA, bold: PDType1Font.HELVETICA_BOLD, italic: PDType1Font.HELVETICA_OBLIQUE, boldItalic: PDType1Font.HELVETICA_BOLD_OBLIQUE],
        'Courier':
            [regular: PDType1Font.COURIER, bold: PDType1Font.COURIER_BOLD, italic: PDType1Font.COURIER_OBLIQUE, boldItalic: PDType1Font.COURIER_BOLD_OBLIQUE],
         'Symbol':
            [regular: PDType1Font.SYMBOL],
        'Digbat':
            [regular: PDType1Font.ZAPF_DINGBATS]
    ]

    static PDFont getFont(Font font) {
        PDFont pdfFont = PDType1Font.HELVETICA
        if (!font?.family) {
            return pdfFont
        }

        def fontOptions = fonts[font.family]
        if (fontOptions) {
            pdfFont = fontOptions.regular

            if (font.italic && font.bold) {
                pdfFont = fontOptions.containsKey('boldItalic') ? fontOptions.boldItalic : pdfFont
            }
            else if (font.italic) {
                pdfFont = fontOptions.containsKey('italic') ? fontOptions.italic : pdfFont
            }
            else if (font.bold) {
                pdfFont = fontOptions.containsKey('bold') ? fontOptions.bold : pdfFont
            }
        }

        pdfFont
    }


}
