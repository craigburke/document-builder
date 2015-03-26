package com.craigburke.document.core.builder

trait TextBlockBuilder {

    def addTextBlockToDocument

	def addTextToTextBlock
	def addImageToTextBlock
	def addLineBreakToTextBlock

	def onTextBlockComplete
}