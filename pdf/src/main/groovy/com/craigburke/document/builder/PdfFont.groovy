package com.craigburke.document.builder

import com.craigburke.document.core.EmbeddedFont
import com.craigburke.document.core.Font
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font

/**
 * Class to load and retrieve PDFonts
 * @author Craig Burke
 */
class PdfFont {

    private static final DEFAULT_FONT = PDType1Font.HELVETICA

    private static fonts = [
            'Times-Roman': [regular: PDType1Font.TIMES_ROMAN, bold: PDType1Font.TIMES_BOLD,
                            italic : PDType1Font.TIMES_ITALIC, boldItalic: PDType1Font.TIMES_BOLD_ITALIC],
            'Helvetica'  : [regular: PDType1Font.HELVETICA, bold: PDType1Font.HELVETICA_BOLD,
                            italic : PDType1Font.HELVETICA_OBLIQUE, boldItalic: PDType1Font.HELVETICA_BOLD_OBLIQUE],
            'Courier'    : [regular: PDType1Font.COURIER, bold: PDType1Font.COURIER_BOLD,
                            italic : PDType1Font.COURIER_OBLIQUE, boldItalic: PDType1Font.COURIER_BOLD_OBLIQUE],
            'Symbol'     : [regular: PDType1Font.SYMBOL],
            'Dingbat'    : [regular: PDType1Font.ZAPF_DINGBATS]
    ]

    static PDFont getFont(Font font) {
        if (!font?.family || !fonts.containsKey(font.family)) {
            return DEFAULT_FONT
        }

        def fontOptions = fonts[font.family]
        PDFont pdfFont = fontOptions.containsKey('regular') ? fontOptions.regular : DEFAULT_FONT

        if (fontOptions) {
            if (font.italic && font.bold) {
                pdfFont = fontOptions.containsKey('boldItalic') ? fontOptions.boldItalic : pdfFont
            } else if (font.italic) {
                pdfFont = fontOptions.containsKey('italic') ? fontOptions.italic : pdfFont
            } else if (font.bold) {
                pdfFont = fontOptions.containsKey('bold') ? fontOptions.bold : pdfFont
            }
        }

        pdfFont
    }

    static BigDecimal getXHeight(Font font) {
        PDFont pdFont = PdfFont.getFont(font)
        (font.size * pdFont.getHeight('x'.bytes[0]) / 1000f)
    }

    static void addFont(PDDocument document, EmbeddedFont embeddedFont) {
        PDFont font = null
        if (embeddedFont.file) {
            font = PDType0Font.load(document, embeddedFont.file)
        }
        else {
            font = PDType0Font.load(document, embeddedFont.inputStream)
        }
        String fontName = embeddedFont.name ?: font.baseFont

        fonts[fontName] = fonts[fontName] ?: [:]
        if (embeddedFont.bold && embeddedFont.italic) {
            fonts[fontName].boldItalic = font
        } else if (embeddedFont.bold) {
            fonts[fontName].bold = font
        } else if (embeddedFont.italic) {
            fonts[fontName].italic = font
        } else {
            fonts[fontName].regular = font
        }

        fonts[fontName].regular = fonts[fontName].regular ?: font
    }

}
