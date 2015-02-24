package com.craigburke.document.builder.render

import com.craigburke.document.core.Text
import org.apache.pdfbox.pdmodel.font.PDFont

/**
 * Rendering element for the Text node
 * @author Craig Burke
 */
class TextElement {
    PDFont pdfFont
    Text node
    String text
    int width
}
