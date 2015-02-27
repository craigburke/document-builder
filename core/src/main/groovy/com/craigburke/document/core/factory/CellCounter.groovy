package com.craigburke.document.core.factory

/**
 * Determines the number of children within a table
 * @author Craig Burke
 */
class CellCounter {
    int totalCount = 0
    private int currentRowCount = 0

    def methodMissing(String name, args) {

        if (name == 'row') {
            currentRowCount = 0
            Closure rowClosure = args.last().clone()
            rowClosure.delegate = this
            rowClosure.resolveStrategy = Closure.DELEGATE_ONLY
            rowClosure()
        }
        else if (name == 'cell') {
            currentRowCount++
            totalCount = Math.max(totalCount, currentRowCount)
        }

    }

    def propertyMissing(String name) { [:] }
}
