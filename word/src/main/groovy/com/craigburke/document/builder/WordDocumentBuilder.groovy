package com.craigburke.document.builder

import com.craigburke.document.core.Cell
import com.craigburke.document.core.Row

import static com.craigburke.document.core.UnitUtil.pointToEmu
import static com.craigburke.document.core.UnitUtil.pointToTwip
import static com.craigburke.document.core.UnitUtil.pointToHalfPoint

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

		wordDocument.generate { builder ->
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

	void addParagraph(builder, Paragraph paragraph) {
		builder.w.p {
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

	void addImageRun(builder, Image image) {
		String blipId = document.item.addImage(image.name, image.data)

		int widthInEmu = pointToEmu(image.width)
		int heightInEmu = pointToEmu(image.height)
		String imageDescription = "Image: ${image.name}"

		builder.w.r {
			w.drawing {
				wp.inline(distT:0, distR:0, distB:0, distL:0) {
					wp.extent(cx:widthInEmu, cy:heightInEmu)
					wp.docPr(id:1, name:imageDescription, descr:image.name)
					a.graphic {
						a.graphicData(uri: "http://schemas.openxmlformats.org/drawingml/2006/picture") {
							pic.pic {
								pic.nvPicPr {
									pic.cNvPr(id:0, name:imageDescription, descr:image.name)
									pic.cNvPicPr {
										a.picLocks(noChangeAspect: 'true')
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
			table.children.each { Row row ->
				w.tr {
					row.children.each { Cell cell ->
						w.tc()
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
