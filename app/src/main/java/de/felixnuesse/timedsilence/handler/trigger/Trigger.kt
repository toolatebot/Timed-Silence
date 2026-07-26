package de.felixnuesse.timedsilence.handler.trigger

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import de.felixnuesse.timedsilence.Constants
import de.felixnuesse.timedsilence.R
import de.felixnuesse.timedsilence.extensions.TAG
import de.felixnuesse.timedsilence.handler.PreferencesManager
import de.felixnuesse.timedsilence.receiver.AlarmBroadcastReciever
import de.felixnuesse.timedsilence.ui.notifications.ErrorNotifications
import de.felixnuesse.timedsilence.ui.notifications.PausedNotification
import de.felixnuesse.timedsilence.util.DateUtil
import de.felixnuesse.timedsilence.volumestate.StateGenerator
import timber.log.Timber
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date


/**
 * Copyright (C) 2019  Felix Nüsse
 * Created on 10.04.19 - 12:00
 *
 * Edited by: Felix Nüsse felix.nuesse(at)t-online.de
 *
 *
 * This program is released under the GPLv3 license
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 *
 *
 */


class Trigger(var mContext: Context) {

    fun removeTimecheck() {
        val alarms = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createBroadcast(0L)?.let { alarms.cancel(it) }
        createBroadcast(0L)?.cancel()

        if(!checkIfNextAlarmExists()){
            Timber.tag(TAG()).d("AlarmHandler: Recurring alarm canceled")
            return
        }
        Timber.tag(TAG()).e("AlarmHandler: Error canceling recurring alarm!")
    }

    fun createBroadcast(targettime: Long): PendingIntent? {
        return createBroadcast(FLAG_IMMUTABLE, targettime)
    }

    fun createBroadcast(flag: Int, targettime: Long): PendingIntent? {

        val broadcastIntent = Intent(mContext, AlarmBroadcastReciever::class.java)

        broadcastIntent.putExtra(
            Constants.BROADCAST_INTENT_ACTION_DELAY_EXTRA,
            Constants.BROADCAST_INTENT_ACTION_DELAY_RESTART_NOW
        )
        broadcastIntent.putExtra(
            Constants.BROADCAST_INTENT_ACTION,
            Constants.BROADCAST_INTENT_ACTION_UPDATE_VOLUME
        )
        broadcastIntent.putExtra(
            Constants.BROADCAST_INTENT_ACTION_TARGET_TIME,
            targettime
        )

        // The Pending Intent to pass in AlarmManager
        return PendingIntent.getBroadcast(mContext,0, broadcastIntent, flag or FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
    }

    fun createAlarmIntime(){
        val now = System.currentTimeMillis()
        val list = StateGenerator(mContext).states()

        val midnight: LocalTime = LocalTime.MIDNIGHT
        val today: LocalDate = LocalDate.now(ZoneId.systemDefault())
        val todayMidnight = LocalDateTime.of(today, midnight)
        val tomorrowMidnight = todayMidnight.plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        var calculatedChecktime = tomorrowMidnight

        for (it in list) {

            var timecheck = it.startTime
            Timber.tag(TAG()).e("Checking time ${it.startTime} ${todayMidnight} ${it.getReason()}")
            Timber.tag(TAG()).e("Calculated time ${DateUtil.getDate(calculatedChecktime)}")
            if(timecheck > now && calculatedChecktime == tomorrowMidnight){
                calculatedChecktime = timecheck
            }
        }
        Timber.tag(TAG()).e("Calculated time $calculatedChecktime")
        Timber.tag(TAG()).e("Calculated time ${DateUtil.getDate(calculatedChecktime)}")

        Timber.tag(TAG()).d("Create new Alarm: $calculatedChecktime,${DateUtil.getDate(calculatedChecktime)}")

        val s = StringBuilder()
        list.forEach { s.append(it.toString()+"\n") }
        Timber.tag(TAG()).d("TargetedAlarmHandler: ${s.toString()}")

        val am = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi: PendingIntent? = createBroadcast(calculatedChecktime)

        if(pi == null) {
            ErrorNotifications().showError(mContext, mContext.getString(R.string.notifications_error_title),  mContext.getString(R.string.notifications_error_description))
            return
        }
        am.cancel(pi)



        val allowWhileIdle = PreferencesManager(mContext).runWhenIdle()

        //todo: fix permission requesting
        if (allowWhileIdle) {
            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calculatedChecktime,
                pi
            )
        } else {
            am.setExact(
                AlarmManager.RTC_WAKEUP,
                calculatedChecktime,
                pi
            )
        }
    }


    fun checkIfNextAlarmExists(): Boolean {
        val pIntent = createBroadcast(PendingIntent.FLAG_NO_CREATE, 0L)
        return if (pIntent == null) {
            Timber.tag(TAG()).d("TriggerInterface: There is no next Alarm set!")
            PausedNotification.show(mContext)
            false
        } else {
            Timber.tag(TAG()).d("TriggerInterface: There is an upcoming Alarm!")
            PausedNotification.cancelNotification(mContext)
            true
        }
    }

    fun getNextAlarmTimestamp(): String {
        val alarms = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val clockInfo = alarms.nextAlarmClock ?: return mContext.getString(R.string.no_next_time_set)

        Timber.tag(TAG())
            .d("TriggerInterface: Next Runtime: " + DateUtil.getDate(clockInfo.triggerTime))
        return DateFormat.getDateInstance().format(Date(clockInfo.triggerTime))

    }

}
