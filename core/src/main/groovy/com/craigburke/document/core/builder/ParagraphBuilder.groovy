package com.craigburke.document.core.builder

import com.craigburke.document.core.LineBreak
import com.craigburke.document.core.Paragraph
import com.craigburke.document.core.Text
import com.craigburke.document.core.Image

trait ParagraphBuilder {
	
	abstract void addTextToParagraph(Text text, Paragraph paragraph)
	abstract void addImageToParagraph(Image image, Paragraph paragraph)
	abstract void addLineBreakToParagraph(LineBreak lineBreak, Paragraph paragraph)
	
	def onParagraphComplete
}