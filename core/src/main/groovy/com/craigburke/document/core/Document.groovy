package com.craigburke.document.core

import static com.craigburke.document.core.UnitUtil.inchToPoint

/**
 * Document node
 * @author Craig Burke
 */
class Document extends BaseNode implements BlockNode, StyledNode {
    final static DEFAULT_MARGIN = new Margin(top:72, bottom:72, left:72, right:72)

    int pageCount
    final int width = inchToPoint(8.5)
    final int height = inchToPoint(11)

    Map template
    def header
    def footer

    List children = []
    List<EmbeddedFont> embeddedFonts = []

    void applyStyles(StyledNode node) {
        if (!template) {
            return
        }

        String className = node.getClass().simpleName.toLowerCase()
        def potentialKeys = [className]
        if (node instanceof Heading) {
            potentialKeys << "heading${node.level}"
        }
        if (node.style) {
            potentialKeys << "${className}.${node.style}"
            if (node instanceof Heading) {
                potentialKeys << "heading${node.level}.${node.style}"
            }
        }

        potentialKeys.each { String key ->
            if (template.containsKey(key) && template[key].font) {
                node.font << template[key].font
            }
        }

    }

}
