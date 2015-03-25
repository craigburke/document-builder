package com.craigburke.document.builder

import static com.craigburke.document.core.UnitUtil.pointToEigthPoint
import static com.craigburke.document.core.UnitUtil.pointToEmu
import static com.craigburke.document.core.UnitUtil.pointToTwip
import static com.craigburke.document.core.UnitUtil.pointToHalfPoint

import com.craigburke.document.core.builder.RenderState
import com.craigburke.document.core.BlockNode
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Row

import com.craigburke.document.core.Image
import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.PageBreak
import com.craigburke.document.core.Paragraph
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

	void initializeDocument(Document document, OutputStream out) {
		document.item = new WordDocument(out)
	}

	void writeDocument(Document document, OutputStream out) {
		WordDocument wordDocument = document.item

		String headerId
		if (document.header) {
			renderState = RenderState.HEADER
			def headerNode = document.header()
			headerId = wordDocument.generateHeader { builder ->
				w.hdr {
					if (headerNode instanceof Paragraph) {
						addParagraph(builder, headerNode)
					}
					else {
						addTable(builder, headerNode)
					}
				}
			}
			renderState = RenderState.PAGE
		}

		String footerId
		if (document.footer) {
			renderState = RenderState.FOOTER
			def footerNode = document.footer()
			footerId = wordDocument.generateFooter { builder ->
				w.hdr {
					if (footerNode instanceof Paragraph) {
						addParagraph(builder, footerNode)
					}
					else {
						addTable(builder, footerNode)
					}
				}
			}
			renderState = RenderState.PAGE
		}

		wordDocument.generateDocument { builder ->
			w.document {
				w.body {
					document.children.each { child ->
						switch (child.getClass()) {
							case Paragraph:
								addParagraph(builder, child)
								break
							case Table:
								addTable(builder, child)
								break
							case PageBreak:
								addPageBreak(builder)
								break
						}
					}
					w.sectPr {
						w.pgMar('w:bottom':pointToTwip(document.margin.bottom),
								'w:top':pointToTwip(document.margin.top),
								'w:right':pointToTwip(document.margin.right),
								'w:left':pointToTwip(document.margin.left))
						if (headerId) {
							w.headerReference('r:id':headerId, 'w:type':'default')
						}
						if (footerId) {
							w.footerReference('r:id':footerId, 'w:type':'default')
						}
					}
				}
			}
		}

		document.item.write()
	}

	void addPageBreak(builder) {
		builder.w.p {
			w.r {
				w.br('w:type':'page')
			}
		}
	}

	int calculateSpacingAfter(BlockNode node) {
		int totalSpacing = node.margin.bottom

		def items = node.parent.children
		int index = items.findIndexOf { it == node }

		if (index != items.size() - 1) {
			def nextSibling = items[index + 1]
			if (nextSibling instanceof BlockNode) {
				totalSpacing += nextSibling.margin.top
			}
		}
		totalSpacing
	}

	void addParagraph(builder, Paragraph paragraph) {
		builder.w.p {
			w.pPr {
				w.spacing('w:before':pointToTwip(paragraph.margin.top), 'w:after':calculateSpacingAfter(paragraph))
				w.ind(	'w:left':pointToTwip(paragraph.margin.left),
						'w:right':pointToTwip(paragraph.margin.right))
				w.jc('w:val':paragraph.align.value)
			}
			paragraph.children.each { child ->
				switch (child.getClass()) {
					case Text:
						addTextRun(builder, child)
						break
					case Image:
						addImageRun(builder, child)
						break
					case LineBreak:
						addLineBreakRun(builder)
						break
				}
			}
		}
	}

	void addLineBreakRun(builder) {
		builder.w.r {
			w.cr()
		}
	}

	DocumentPartType getCurrentDocumentPart() {
		switch (renderState) {
			case RenderState.PAGE:
				DocumentPartType.DOCUMENT
				break
			case RenderState.HEADER:
				DocumentPartType.HEADER
				break
			case RenderState.FOOTER:
				DocumentPartType.FOOTER
				break
		}
	}

	void addImageRun(builder, Image image) {
		String blipId = document.item.addImage(image.name, image.data, currentDocumentPart)

		int widthInEmu = pointToEmu(image.width)
		int heightInEmu = pointToEmu(image.height)
		String imageDescription = "Image: ${image.name}"

		builder.w.r {
			w.drawing {
				wp.inline(distT:0, distR:0, distB:0, distL:0) {
					wp.extent(cx:widthInEmu, cy:heightInEmu)
					wp.docPr(id:1, name:imageDescription, descr:image.name)
					a.graphic {
						a.graphicData(uri:'http://schemas.openxmlformats.org/drawingml/2006/picture') {
							pic.pic {
								pic.nvPicPr {
									pic.cNvPr(id:0, name:imageDescription, descr:image.name)
									pic.cNvPicPr {
										a.picLocks(noChangeAspect:'true')
									}
								}
								pic.blipFill {
									a.blip('r:embed':blipId)
									a.stretch {
										a.fillRect()
									}
								}
								pic.spPr {
									a.xfrm {
										a.off(x:0, y:0)
										a.ext(cx:widthInEmu, cy:heightInEmu)
									}
									a.prstGeom(prst:'rect') {
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
				w.tblW('w:w':pointToTwip(table.width))
				w.tblBorders {
					def properties = ['top', 'right', 'bottom', 'left', 'insideH', 'insideV']
					properties.each { String property ->
						w."${property}"(
							'w:sz':pointToEigthPoint(table.border.size),
							'w:color':table.border.color.hex,
							'w:val':(table.border.size == 0 ? 'none' : 'single')
						)
					}
				}
			}

			table.children.each { Row row ->
				w.tr {
					row.children.each { Cell cell ->
						w.tc {
							w.tcPr {
								w.tcW('w:w':pointToTwip(cell.width - (table.padding * 2)))
								w.tcMar {
									w.top('w:w':pointToTwip(table.padding))
									w.bottom('w:w':pointToTwip(table.padding))
									w.left('w:w':pointToTwip(table.padding))
									w.right('w:w':pointToTwip(table.padding))
								}
							}
							cell.children.each { addParagraph(builder, it) }
						}
					}
				}
			}
		}
	}

	void addTextRun(builder, Text text) {
		builder.w.r {
			w.rPr {
				w.rFonts('w:ascii':text.font.family)
				if (text.font.bold) {
					w.b()
				}
				if (text.font.italic) {
					w.i()
				}
				w.color('w:val':text.font.color.hex)
				w.sz('w:val':pointToHalfPoint(text.font.size))
			}
			w.t(text.value)
		}
	}

}
