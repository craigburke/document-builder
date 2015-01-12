package com.craigburke.document.core

class Margin {
    
    BigDecimal top
    BigDecimal bottom
    BigDecimal left
    BigDecimal right
    
    void setDefault(BigDecimal defaultValue) {
        ['top', 'bottom', 'left', 'right'].each { property ->
            if (this."${property}" == null) {
                this."${property}" = defaultValue
            }
        }
        
    }
    
}
