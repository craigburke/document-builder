package com.craigburke.document.core

import groovy.transform.AutoClone

@AutoClone
class Font {
	String family = "Helvetica"
	Integer size = 14
	
	Boolean bold = false
	Boolean italic = false
	Boolean dropCap = false

	String hexColor = '000000'
	def rgbColor = [0, 0, 0]
	
	void setColor(String color) {
		if (color.startsWith('#')) {
			hexColor = color[1..-1]
			rgbColor = (hexColor =~ /.{2}/).collect { Integer.parseInt(it, 16) }
		}
	}
		
	def leftShift(Map properties) {
		properties.each { key, value -> this[key] = value }
	} 	
		
	void setHexColor(color) { }
	void setRgbColor(color) { }
}