package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument

/**
 * Trait shared by render elements
 */
trait Renderable {
    float startX
    int renderCount = 0
    PdfDocument pdfDocument
    
    abstract void parse(float maxHeight)
    abstract boolean getFullyParsed()
    abstract float getTotalHeight()
    abstract float getParsedHeight()
    abstract void renderElement(float startY)

    void render(float startY) {
        renderCount = renderCount + 1
        float currentX = pdfDocument.x
        float currentY = pdfDocument.y        
        pdfDocument.y = startY
        pdfDocument.x = startX
        renderElement(startY)
        pdfDocument.x = currentX
        pdfDocument.y = currentY
    }
    
    boolean getOnFirstPage() {
        renderCount <= 1
    }
    
}
