package com.craigburke.document.core.test

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.Document
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll

abstract class DocumentBuilderSpec extends Specification {

	@Shared ByteArrayOutputStream out
	@Shared DocumentBuilder builder
	@Shared MARGINS = [
			[top: 0, bottom: 0, left: 0, right: 0],
			[top: 2 * 72, bottom: 3 * 72, left: 1.25 * 72, right: 2.5 * 72],
			[top: 72 / 4, bottom: 72 / 2, left: 72 / 4, right: 72 / 2]
	]

	byte[] getData() { out.toByteArray() }

	abstract DocumentBuilder createBuilderInstance(OutputStream out)
	abstract Document getDocument(byte[] data)

	def setup() {
		out = new ByteArrayOutputStream()
		builder = createBuilderInstance(out)
	}
	
	def "create empty document"() {
		when:
		builder.create { document() }
		
		then:
		builder.document != null
		
		and:
		builder.document.item != null
	}
	
	def "load fonts"() {
		setup:
		String fontFileName = "OpenSans-Bold.ttf"

		File fontFolder = new File("temp-fonts")
		File fontFile = new File(fontFolder, fontFileName)
		fontFolder.mkdirs()

		fontFile << DocumentBuilderSpec.classLoader.getResourceAsStream("test/fonts/${fontFileName}")
				
		when:
		builder.create { document {
			addFont fontFile
			addFontFolder fontFolder
		}}
		
		then:
		notThrown(Exception)

		cleanup:
		fontFile?.delete()
		fontFolder?.deleteDir()
	}

	@Unroll
	def "set document margins"() {
		when:
		builder.create { document(margin : [top: margin.top, bottom: margin.bottom, left: margin.left, right: margin.right] ) {
			paragraph "Content"
		}}
		
		def document = getDocument(data)

		then:
		document.margin.left == margin.left

		and:
		document.margin.right == margin.right

		and:
		document.margin.top == margin.top
		
		and:
		document.margin.bottom == margin.bottom

		where:
		margin << MARGINS
	}

	@Unroll
	def "set paragraph margins"() {
		when:
		builder.create { document() {
			paragraph(margin: [top: currentMargin.top, bottom: currentMargin.bottom, left: currentMargin.left, right: currentMargin.right]) {
				font.size = fontSize
				text "Foo"
			}
		}}
		
		def paragraph = getDocument(data).children[0]

		then:
		paragraph.margin.left == currentMargin.left
		
		and:
		paragraph.margin.right >= currentMargin.right

		and:
		paragraph.margin.top == currentMargin.top

		where:
		fontSize << [12, 60, 120]
		currentMargin << MARGINS
	}

	def "override or inherit font settings"() {
		when:
		builder.create { document(font: [family: 'Helvetica']) {
			
			paragraph(font: [family: 'Courier']) {
				text "Paragraph override"
			}
			paragraph "Inherit doc font"

			paragraph {
				text "Text override", font: [family: 'Times-Roman']
			}

			table(font: [family: 'Courier']) {
				row {
					cell("Override")
				}
			}
			table {
				row {
					cell("Default font")
				}
			}
			
		}}
		
		def document = getDocument(data)
		
		def paragraph1 = document.children[0].children[0]
		def paragraph2 = document.children[1].children[0]
		def paragraph3 = document.children[2].children[0]

		def table1 = document.children[3].rows[0].cells[0].paragraphs[0].children[0]
		def table2 = document.children[4].rows[0].cells[0].paragraphs[0].children[0]

		then:
		paragraph1.font.family == 'Courier'
		
		and:
		paragraph2.font.family == 'Helvetica'

		and:
		paragraph3.font.family == 'Times-Roman'

		and:
		table1.font.family == 'Courier'
		
		and:
		table2.font.family == 'Helvetica'
	}

	def "create table without a paragraph"() {
		when:
		builder.create { document {
			table {
				row {
					cell {
						text "FOOBAR"
					}
				}
			}
		}}

		def table = getDocument(data).children[0]
		
		then:
		table.rows[0].cells[0].paragraphs[0].text == "FOOBAR"

	}
	
	def "set table options"() {
		when:
		builder.create { document {
			table(width: 4.inches, borderSize: 4) {
				row {
					cell("Cell 1", width: 1.inch)
					cell("Cell 2", width: 3.inches)
				}
			}			
		}}
		
		def table = getDocument(data).children[0]
	
		then:
		table.width == 288
		
		and:
		table.rows[0].cells[0].width == 72
		
		and:
		table.rows[0].cells[1].width == 216
	}

	def "set paragraph text"() {
		when:
		builder.create { document {
			paragraph "Foo"
			paragraph("Foo") {
				text "Ba"
				text "r"
			}
			paragraph {
				text "B"
				text "a"
				text "r"
			}
		}}
		
		def paragraphs = getDocument(data).children
		
		then:		
		paragraphs[0].text == "Foo"
		
		and:
		paragraphs[1].text == "FooBar"
		
		and:
		paragraphs[2].text == "Bar"
	}	
	
	def "create a table with multiple cells"() {
		when:
		builder.create { document {
			table {
				row {
					cell("Cell1")
					cell("Cell2")
					cell {
						paragraph "Cell3"
					}
				}

			}
		}}

		def table = getDocument(data).children[0]

		then:
		notThrown(Exception)
	}

	def "add an image"() {
		def imageData = getClass().classLoader.getResource('test/images/cheeseburger.jpg')?.bytes
		
		given:
		imageData != null
		
		when:
		builder.create { document {
			paragraph {
				image(data: imageData, width: 500.px, height: 431.px)
			}
		}}
		
		def image = getDocument(data).children[0].children[0]
		
		then:
		image.data == imageData
	}
	
	
	
}