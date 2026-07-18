package de.felixnuesse.timedsilence.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.felixnuesse.timedsilence.handler.PreferencesManager
import de.felixnuesse.timedsilence.handler.trigger.Trigger
import de.felixnuesse.timedsilence.handler.volume.VolumeHandler
import de.felixnuesse.timedsilence.volumestate.StateGenerator
import de.felixnuesse.timedsilence.extensions.TAG
import timber.log.Timber

class BootReciever : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action==Intent.ACTION_BOOT_COMPLETED){
            Timber.tag(TAG()).e("BootReciever: Started Device!")
            VolumeHandler(context, "BootReciever").setVolumeStateAndApply(StateGenerator(context).stateAt(System.currentTimeMillis()))

            val prefs = PreferencesManager(context)

            if(prefs.shouldRestartOnBoot() || prefs.forceRestartOnBoot()){
                Timber.tag(TAG()).e("BootReciever: Started Checks!")
                Trigger(context).createAlarmIntime()
                return
            }
            Timber.tag(TAG()).e("BootReciever: Don't check.")
        }
    }


}
