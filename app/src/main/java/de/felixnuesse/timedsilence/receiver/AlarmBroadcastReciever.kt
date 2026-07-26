package de.felixnuesse.timedsilence.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import de.felixnuesse.timedsilence.Constants
import de.felixnuesse.timedsilence.extensions.TAG
import de.felixnuesse.timedsilence.handler.trigger.Trigger
import de.felixnuesse.timedsilence.handler.volume.VolumeHandler
import de.felixnuesse.timedsilence.handler.volume.VolumeService
import de.felixnuesse.timedsilence.util.DateUtil
import de.felixnuesse.timedsilence.volumestate.StateGenerator
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class AlarmBroadcastReciever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        val targetTime = intent?.getLongExtra(Constants.BROADCAST_INTENT_ACTION_TARGET_TIME, 0L)?: 0L
        // Precalculate, because writing to disk actually takes time. (In testing, 15ms)
        val diff = System.currentTimeMillis()-targetTime
        val duration = DateUtil.getDelta(targetTime, System.currentTimeMillis())
        var executionDelay = 1000L

        Timber.tag(TAG()).d("Alarm Recieved - Was targeted at: $targetTime")

        Timber.tag(TAG()).d(
            "Alarm Recieved - Timediff: $duration ($diff ms)"
        )

        if(diff <= 0) {
            Timber.tag(TAG()).d(
                "Alarm Recieved - Danger! We are before the scheduled time! (${diff*-1} ms before)"
            )
            Timber.tag(TAG()).d(
                "Alarm Recieved - Deliberately delay another ${(diff*-1)+100}ms to reach time."
            )
            executionDelay+=(diff*-1)+100;
        }

        val r = Runnable {
            Timber.tag(TAG()).d("Alarmintent: Starting VolumeService at: ${DateUtil.getDate()}")
            val serviceIntent = Intent(context, VolumeService::class.java).apply {
                putExtra(Constants.BROADCAST_INTENT_ACTION, intent?.getStringExtra(Constants.BROADCAST_INTENT_ACTION))
                putExtra(Constants.BROADCAST_INTENT_ACTION_DELAY_EXTRA, intent?.getStringExtra(Constants.BROADCAST_INTENT_ACTION_DELAY_EXTRA))
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        val scheduledExecutor = Executors.newScheduledThreadPool(1)
        scheduledExecutor.schedule(r, executionDelay, TimeUnit.MILLISECONDS)


    }


}