package com.craigburke.document.core.builder

import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import com.craigburke.document.core.Image

trait ParagraphBuilder {

    def addParagraphToDocument

	def addTextToParagraph
	def addImageToParagraph
	def addLineBreakToParagraph

	def onParagraphComplete
}