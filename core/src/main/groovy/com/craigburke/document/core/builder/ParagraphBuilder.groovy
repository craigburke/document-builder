package com.craigburke.document.core.builder

trait ParagraphBuilder {

    def addParagraphToDocument

	def addTextToParagraph
	def addImageToParagraph
	def addLineBreakToParagraph

	def onParagraphComplete
}