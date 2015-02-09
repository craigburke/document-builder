package com.craigburke.document.core

class UnitUtil {
	
	static BigDecimal inchToPoint(BigDecimal inch) {
		inch * 72
	}
	
	static BigDecimal pointToInch(BigDecimal point) {
		point / 72
	}

	static BigDecimal pointToEigthPoint(BigDecimal point) {
		point * 8
	}
	
	static BigDecimal pointToPica(BigDecimal point) {
		point * 6
	}
	
	static BigDecimal picaToPoint(BigDecimal pica) {
		pica / 6
	}
	
	static BigDecimal eightPointToPoint(BigDecimal eigthPoint) {
		eigthPoint / 8
	}
	
	static BigDecimal pointToTwip(BigDecimal point) {
		point * 20
	}
	
	static BigDecimal twipToPoint(BigDecimal twentiethPoint) {
		twentiethPoint / 20
	}
	
}
