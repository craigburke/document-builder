package com.craigburke.document.builder

import java.text.SimpleDateFormat

import static com.craigburke.document.core.UnitUtil.pointToEigthPoint
import static com.craigburke.document.core.UnitUtil.pointToEmu
import static com.craigburke.document.core.UnitUtil.pointToTwip
import static com.craigburke.document.core.UnitUtil.pointToHalfPoint

import com.craigburke.document.core.HeaderFooterOptions
import com.craigburke.document.core.Heading
import com.craigburke.document.core.builder.RenderState
import com.craigburke.document.core.BlockNode
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Row
import com.craigburke.document.core.Font
import com.craigburke.document.core.Image
import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.PageBreak
import com.craigburke.document.core.TextBlock
import com.craigburke.document.core.Table
import com.craigburke.document.core.Text
import groovy.transform.InheritConstructors

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.Document

/**
 * Builder for Word documents
 * @author Craig Burke
 */
@InheritConstructors
class WordDocumentBuilder extends DocumentBuilder {

    private static final String CREATOR = 'Groovy Document Builder'
    private static final String PAGE_NUMBER_PLACEHOLDER = '##pageNumber##'
    private static final Map RUN_TEXT_OPTIONS = ['xml:space': 'preserve']

    void initializeDocument(Document document, OutputStream out) {
        document.element = new WordDocument(out)
    }

    WordDocument getWordDocument() {
        document.element
    }

    void writeDocument(Document document, OutputStream out) {
        def headerFooterOptions = new HeaderFooterOptions(
                pageNumber: PAGE_NUMBER_PLACEHOLDER,
                pageCount: document.pageCount,
                dateGenerated: new Date()
        )

        renderCoreProperties(document.metadata)
        renderAppProperties(document.metadata)
        renderSettings()

        def header = renderHeader(headerFooterOptions)
        def footer = renderFooter(headerFooterOptions)

        renderState = RenderState.PAGE
        wordDocument.generateDocument { builder ->
            w.document {
                w.body {
                    document.children.each { child ->
                        if (child instanceof TextBlock) {
                            addParagraph(builder, child)
                        } else if (child instanceof PageBreak) {
                            addPageBreak(builder)
                        } else if (child instanceof Table) {
                            addTable(builder, child)
                        }
                    }
                    w.sectPr {
                        w.pgSz('w:h': pointToTwip(document.height),
                                'w:w': pointToTwip(document.width),
                                'w:orient': 'portrait'
                        )
                        w.pgMar('w:bottom': pointToTwip(document.margin.bottom),
                                'w:top': pointToTwip(document.margin.top),
                                'w:right': pointToTwip(document.margin.right),
                                'w:left': pointToTwip(document.margin.left),
                                'w:footer': pointToTwip(footer ? footer.node.margin.bottom : 0),
                                'w:header': pointToTwip(header ? header.node.margin.top : 0)
                        )
                        if (header) {
                            w.headerReference('r:id': header.id, 'w:type': 'default')
                        }
                        if (footer) {
                            w.footerReference('r:id': footer.id, 'w:type': 'default')
                        }
                    }
                }
            }
        }

        renderState = RenderState.CUSTOM
        wordDocument.write()
    }

    private String formatDate(Date date) {
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        dateFormat.setTimeZone(TimeZone.getTimeZone('GMT'))
        dateFormat.format(new Date())
    }

    void renderCoreProperties(Map metadata) {
        def now = formatDate(new Date())

        wordDocument.generateDocumentProperties(BasicDocumentPartTypes.CORE_PROPERTIES) { builder ->
            cp.coreProperties {
                if (metadata.title) {
                    dc.title(metadata.title)
                }
                if (metadata.subject) {
                    dc.subject(metadata.subject)
                }
                dc.creator(metadata.author ?: metadata.creator ?: CREATOR)
                if (metadata.keywords) {
                    cp.keywords(metadata.keywords)
                }
                if (metadata.description) {
                    dc.description(metadata.description)
                }
                if(metadata.lastModifiedBy) {
                    cp.lastModifiedBy(metadata.lastModifiedBy)
                }
                if(metadata.revision != null) {
                    cp.revision(metadata.revision.toString())
                }
                dcterms.created('xsi:type': 'dcterms:W3CDTF') {
                    mkp.yieldUnescaped(metadata.created ?: now)
                }
                dcterms.modified('xsi:type': 'dcterms:W3CDTF') {
                    mkp.yieldUnescaped(metadata.modified ?: now)
                }
                if (metadata.category) {
                    cp.category(metadata.category)
                }
            }
        }
    }

    void renderAppProperties(Map metadata) {
        wordDocument.generateDocumentProperties(BasicDocumentPartTypes.APP_PROPERTIES) { builder ->
            Properties {
                Application(metadata.creator ?: this.CREATOR)
                if (metadata.manager) {
                    Manager(metadata.manager)
                }
                if (metadata.company) {
                    Company(metadata.company)
                }
            }
        }
    }

    def renderSettings() {
        def settings = [:]
        settings.id = wordDocument.generateDocumentPart(BasicDocumentPartTypes.SETTINGS) {builder->
            w.settings {
                w.compat {
                    w.compatSetting('w:name': "compatibilityMode", 'w:uri': "http://schemas.microsoft.com/office/word", 'w:val': "14")
                }
            }
        }
        settings
    }

    def renderHeader(HeaderFooterOptions options) {
        def header = [:]
        if (document.header) {
            renderState = RenderState.HEADER
            header.node = document.header(options)
            header.id = wordDocument.generateDocumentPart(BasicDocumentPartTypes.HEADER) { builder ->
                w.hdr {
                    renderHeaderFooterNode(builder, header.node as BlockNode)
                }
            }
        }
        header
    }

    def renderFooter(HeaderFooterOptions options) {
        def footer = [:]
        if (document.footer) {
            renderState = RenderState.FOOTER
            footer.node = document.footer(options)
            footer.id = wordDocument.generateDocumentPart(BasicDocumentPartTypes.FOOTER) { builder ->
                w.hdr {
                    renderHeaderFooterNode(builder, footer.node as BlockNode)
                }
            }
        }
        footer
    }

    void renderHeaderFooterNode(builder, BlockNode node) {
        if (node instanceof TextBlock) {
            addParagraph(builder, node)
        } else {
            addTable(builder, node)
        }

    }

    void addPageBreak(builder) {
        builder.w.p {
            w.r {
                w.br('w:type': 'page')
            }
        }
    }

    int calculateSpacingAfter(BlockNode node) {
        int totalSpacing

        switch (renderState) {
            case RenderState.PAGE:
                totalSpacing = node.margin.bottom

                def items = node.parent.children
                int index = items.findIndexOf { it == node }

                if (index != items.size() - 1) {
                    def nextSibling = items[index + 1]
                    if (nextSibling instanceof BlockNode) {
                        totalSpacing += nextSibling.margin.top
                    }
                }
                break

            case RenderState.HEADER:
                totalSpacing = node.margin.bottom
                break

            case RenderState.FOOTER:
                totalSpacing = 0
        }
        pointToTwip(totalSpacing)
    }

    int calculatedSpacingBefore(BlockNode node) {
        int totalSpacing

        switch (renderState) {
            case RenderState.PAGE:
                totalSpacing = node.margin.top
                def items = node.parent.children
                int index = items.findIndexOf { it == node }
                if (index > 0) {
                    def previousSibling = items[index - 1]
                    if (previousSibling instanceof Table) {
                        totalSpacing += previousSibling.margin.bottom
                    }
                }
                break

            case RenderState.HEADER:
                totalSpacing = 0
                break

            case RenderState.FOOTER:
                totalSpacing = node.margin.top
                break
        }

        pointToTwip(totalSpacing)
    }

    void addParagraph(builder, TextBlock paragraph) {

        builder.w.p {
            w.pPr {

                if (paragraph instanceof Heading && stylesEnabled) {
                    w.pStyle 'w:val': "Heading${paragraph.level}"
                }

                String lineRule = (paragraph.lineSpacing) ? 'exact' : 'auto'
                BigDecimal lineValue = (paragraph.lineSpacing) ?
                        pointToTwip(paragraph.lineSpacing) : (paragraph.lineSpacingMultiplier * 240)
                w.spacing(
                        'w:before': calculatedSpacingBefore(paragraph),
                        'w:after': calculateSpacingAfter(paragraph),
                        'w:lineRule': lineRule,
                        'w:line': lineValue
                )
                w.ind(
                        'w:start': pointToTwip(paragraph.margin.left),
                        'w:left': pointToTwip(paragraph.margin.left),
                        'w:right': pointToTwip(paragraph.margin.right),
                        'w:end': pointToTwip(paragraph.margin.right)
                )
                w.jc('w:val': paragraph.align.value)

                if (paragraph instanceof Heading) {
                    w.outlineLvl('w:val': "${paragraph.level - 1}")
                }
            }

            String paragraphLinkId = UUID.randomUUID()
            if (paragraph.ref) {
                w.bookmarkStart('w:id': paragraphLinkId, 'w:name': paragraph.ref)
            }
            paragraph.children.each { child ->
                switch (child.getClass()) {
                    case Text:
                        if (child.url?.startsWith('#') && child.url.size() > 1) {
                            addLink(builder, child)
                        } else if (child.ref) {
                            addBookmark(builder, child)
                        } else {
                            addTextRun(builder, child.font as Font, child.value as String)
                        }
                        break
                    case Image:
                        addImageRun(builder, child)
                        break
                    case LineBreak:
                        addLineBreakRun(builder)
                        break
                }
            }
            if (paragraph.ref) {
                w.bookmarkEnd('w:id': paragraphLinkId)
            }
        }
    }

    protected boolean isStylesEnabled() {
        false
    }

    void addBookmark(builder, Text text) {
        String id = UUID.randomUUID()
        builder.w.bookmarkStart('w:id': id, 'w:name': text.ref)
        addTextRun(builder, text.font as Font, text.value as String)
        builder.w.bookmarkEnd('w:id': id)
    }

    void addLink(builder, Text text) {
        builder.w.hyperlink('w:anchor': text.url[1..-1]) {
            addTextRun(builder, text.font as Font, text.value as String)
        }
    }

    void addLineBreakRun(builder) {
        builder.w.r {
            w.br()
        }
    }

    DocumentPartType getCurrentDocumentPart() {
        switch (renderState) {
            case RenderState.PAGE:
                BasicDocumentPartTypes.DOCUMENT
                break
            case RenderState.HEADER:
                BasicDocumentPartTypes.HEADER
                break
            case RenderState.FOOTER:
                BasicDocumentPartTypes.FOOTER
                break
        }
    }

    void addImageRun(builder, Image image) {
        String blipId = document.element.addImage(image.name, image.data, currentDocumentPart)

        int widthInEmu = pointToEmu(image.width)
        int heightInEmu = pointToEmu(image.height)
        String imageDescription = "Image: ${image.name}"

        builder.w.r {
            w.drawing {
                wp.inline(distT: 0, distR: 0, distB: 0, distL: 0) {
                    wp.extent(cx: widthInEmu, cy: heightInEmu)
                    wp.docPr(id: 1, name: imageDescription, descr: image.name)
                    a.graphic {
                        a.graphicData(uri: 'http://schemas.openxmlformats.org/drawingml/2006/picture') {
                            pic.pic {
                                pic.nvPicPr {
                                    pic.cNvPr(id: 0, name: imageDescription, descr: image.name)
                                    pic.cNvPicPr {
                                        a.picLocks(noChangeAspect: 'true')
                                    }
                                }
                                pic.blipFill {
                                    a.blip('r:embed': blipId)
                                    a.stretch {
                                        a.fillRect()
                                    }
                                }
                                pic.spPr {
                                    a.xfrm {
                                        a.off(x: 0, y: 0)
                                        a.ext(cx: widthInEmu, cy: heightInEmu)
                                    }
                                    a.prstGeom(prst: 'rect') {
                                        a.avLst()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void addTable(builder, Table table) {
        builder.w.tbl {
            w.tblPr {
                w.tblW('w:w': pointToTwip(table.width), 'w:type': 'dxa')
                w.tblBorders {
                    def properties = ['top', 'right', 'bottom', 'left', 'insideH', 'insideV']
                    properties.each { String property ->
                        w."${property}"(
                                'w:sz': pointToEigthPoint(table.border.size),
                                'w:color': table.border.color.hex,
                                'w:val': (table.border.size == 0 ? 'none' : 'single')
                        )
                    }
                }
            }

            if (table.columns) {
                w.tblGrid {
                    List<BigDecimal> columnWidths = table.computeColumnWidths()
                    for (BigDecimal columnWidth in columnWidths) {
                        w.gridCol('w:w': pointToTwip(columnWidth).longValue())
                    }
                }
            }

            table.children.each { Row row ->
                w.tr {
                    row.children.each { Cell column ->
                        if (column.rowsSpanned == 0) {
                            addColumn(builder, column)
                        } else {
                            addMergeColumn(builder)
                        }
                        column.rowsSpanned++
                    }
                }
            }
        }
    }

    void addColumn(builder, Cell column) {
        Table table = column.parent.parent

        builder.w.tc {
            w.tcPr {
                w.vAlign('w:val': 'top')
                w.tcW('w:w': pointToTwip(column.width - (table.padding * 2)), 'w:type': 'dxa')
                w.tcMar {
                    w.top('w:w': pointToTwip(table.padding), 'w:type': 'dxa')
                    w.bottom('w:w': pointToTwip(table.padding), 'w:type': 'dxa')
                    w.left('w:w': pointToTwip(table.padding), 'w:type': 'dxa')
                    w.right('w:w': pointToTwip(table.padding), 'w:type': 'dxa')
                }
                if (column.background) {
                    w.shd('w:val': 'clear', 'w:color': 'auto', 'w:fill': column.background.hex)
                }
                if (column.colspan > 1) {
                    w.gridSpan('w:val': column.colspan)
                }
                if (column.rowspan > 1) {
                    w.vMerge('w:val': 'restart')
                }
            }
            column.children.each {
                if (it instanceof TextBlock) {
                    addParagraph(builder, it)
                } else {
                    addTable(builder, it)
                    w.p()
                }
            }
            if (!column.children) {
                w.p()
            }
        }

    }

    void addMergeColumn(builder) {
        builder.w.tc {
            w.tcPr {
                w.vMerge()
            }
            w.p()
        }
    }

    void addTextRun(builder, Font font, String text) {
        builder.w.r {
            w.rPr {
                w.rFonts('w:ascii': font.family)
                if (font.bold) {
                    w.b()
                }
                if (font.italic) {
                    w.i()
                }
                w.color('w:val': font.color.hex)
                w.sz('w:val': pointToHalfPoint(font.size))
            }
            if (renderState == RenderState.PAGE) {
                w.t(text, RUN_TEXT_OPTIONS)
            } else {
                parseHeaderFooterText(builder, text)
            }
        }
    }

    static void parseHeaderFooterText(builder, String text) {
        def textParts = text.split(PAGE_NUMBER_PLACEHOLDER)
        textParts.eachWithIndex { String part, int index ->
            if (index != 0) {
                builder.w.pgNum()
            }
            builder.w.t(part, RUN_TEXT_OPTIONS)
        }
    }

}
