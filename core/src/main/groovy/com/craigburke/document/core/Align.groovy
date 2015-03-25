package com.craigburke.document.core

enum Align {
    LEFT('left'), 
    RIGHT('right'), 
    CENTER('center')
    
    String value
    
    Align(String value) {
        this.value = value
    }
}
