package com.craigburke.document.core

import static com.craigburke.document.core.UnitUtil.inchToPoint

/**
 * Document node
 * @author Craig Burke
 */
class Document extends BlockNode {
    static Margin defaultMargin = new Margin(top:72, bottom:72, left:72, right:72)

    int pageCount
    final int width = inchToPoint(8.5)
    final int height = inchToPoint(11)

    def template
    def header
    def footer

    private Map templateMap

    Map getTemplateMap() {
        if (templateMap == null) {
            loadTemplateMap()
        }
        templateMap
    }

    private void loadTemplateMap() {
        templateMap = [:]
        if (template && template instanceof Closure) {
            def templateDelegate = new Expando()
            templateDelegate.metaClass.methodMissing = { name, args ->
                templateMap[name] = args[0]
            }
            template.delegate = templateDelegate
            template()
        }
    }

    List children = []
    List<EmbeddedFont> embeddedFonts = []

}
