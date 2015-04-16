package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument

/**
 * Trait shared by render elements
 */
trait Renderable {
    float startX
    PdfDocument pdfDocument
    abstract void parseUntilHeight(float height)
    abstract boolean getFullyParsed()
    abstract float getTotalHeight()
    abstract float getParsedHeight()
    abstract void renderElement(float startY)
    void render(float startY) {
        float currentX = pdfDocument.x
        float currentY = pdfDocument.y        
        pdfDocument.y = startY
        pdfDocument.x = startX
        renderElement(startY)
        pdfDocument.x = currentX
        pdfDocument.y = currentY
    }
    
}
