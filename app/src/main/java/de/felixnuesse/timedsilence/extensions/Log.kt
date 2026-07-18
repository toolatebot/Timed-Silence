package de.felixnuesse.timedsilence.extensions

import timber.log.Timber


fun Any.e(message: String) {
    Timber.tag(TAG()).e(message)
}
fun Any.d(message: String) {
    Timber.tag(TAG()).d(message)
}
fun Any.i(message: String) {
    Timber.tag(TAG()).i(message)
}