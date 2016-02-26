package com.craigburke.document.core

import groovy.transform.AutoClone

/**
 * Block element that holds text and images
 * @author Craig Burke
 */
@AutoClone
class TextBlock extends BlockNode implements Linkable, Bookmarkable, LineSpacingAssignable {
    static Margin defaultMargin = new Margin(top: 12, bottom: 12, left: 0, right: 0)

    List children = []

    String getText() {
        children.findAll { it.getClass() == Text }*.value.join('')
    }

    List addText(String text) {
        List elements = []
        def textSections = text.split('\n')

        textSections.each { String section ->
            elements << new Text(value: section, parent: this)

            if (section != textSections.last()) {
                elements << new LineBreak(parent: this)
            }
        }

        if (text.endsWith('\n')) {
            elements << new LineBreak(parent: this)
        }

        children += elements
        elements
    }
}
