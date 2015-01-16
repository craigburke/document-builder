package com.craigburke.document.core

import groovy.transform.AutoClone

@AutoClone
class Font {
	String family = "Helvetica"
	BigDecimal size = 12
	Boolean bold = false
	Boolean italic = false

	Color color = new Color('#000000')

	def leftShift(Map properties) {
		properties.each { key, value -> this[key] = value }
	} 	

}