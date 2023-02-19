package com.hqnguyen.syl

import timber.log.Timber

class TimberLog : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format(
            "%s:%s",
            super.createStackElementTag(element),
            element.lineNumber
        )
    }
}