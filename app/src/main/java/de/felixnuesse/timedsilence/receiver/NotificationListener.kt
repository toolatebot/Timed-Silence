package de.felixnuesse.timedsilence.receiver

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat
import de.felixnuesse.timedsilence.Constants
import de.felixnuesse.timedsilence.extensions.TAG
import de.felixnuesse.timedsilence.handler.PreferencesManager
import de.felixnuesse.timedsilence.services.VolumeService
import timber.log.Timber


class NotificationListener : NotificationListenerService() {

    companion object {
        private var service: NotificationListener? = null
        fun getService(): NotificationListener? {
            return service
        }
    }

    var allNotifications = arrayListOf<StatusBarNotification>()

    override fun onCreate() {
        super.onCreate()
        service = this
    }

    fun getAllActiveNotifications(): ArrayList<StatusBarNotification> {
        return allNotifications
    }

    override fun onNotificationPosted(notification: StatusBarNotification) {
        if(notification.packageName == this.baseContext.packageName) {
            return
        }
        allNotifications.clear()
        allNotifications.addAll(activeNotifications)
        handleNotification()
    }

    override fun onNotificationRemoved(notification: StatusBarNotification) {
        if(notification.packageName == this.baseContext.packageName) {
            return
        }
        allNotifications.clear()
        allNotifications.addAll(activeNotifications)
        handleNotification()
    }

    private fun handleNotification() {
        val proceed = PreferencesManager(this).shouldSearchInNotifications()
        if(!proceed) {
            return
        }

        Timber.tag(TAG()).e("NotificationListener: Posted or removed notification, check!")
    }
}
