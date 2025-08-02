package com.lfgit.view_models.Events

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * source: https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 */
@Suppress("unused")
class Event<T>(private val mContent: T) {

    private var hasBeenHandled = false

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            mContent
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = mContent
} 