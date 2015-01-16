package com.craigburke.document.core.factory

class CellCounter {
    int totalCount = 0
    int _currentRowCount = 0

    def methodMissing(String name, args) {
        if (name == "row" && args?.last() instanceof Closure) {
            _currentRowCount = 0
            Closure rowClosure = args.last().clone()
            rowClosure.delegate = this
            rowClosure.resolveStrategy = Closure.DELEGATE_ONLY
            rowClosure()
        }
        if (name == "cell") {
            _currentRowCount++
            totalCount = Math.max(totalCount, _currentRowCount)
        }

    }

    def propertyMissing(String name) { [:] }
}
