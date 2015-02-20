package com.craigburke.document.builder.render

import com.craigburke.document.core.Text
import org.apache.pdfbox.pdmodel.font.PDFont

class TextElement {
    PDFont pdfFont
    Text node
    String text
    int width
}
