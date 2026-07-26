package de.felixnuesse.timedsilence.util

import android.content.Context
import android.os.Environment
import android.util.Log
import fr.bipi.treessence.common.formatter.Formatter
import fr.bipi.treessence.file.FileLoggerTree
import timber.log.Timber.Forest
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class LoggingUtils {
    companion object {

        private var KB = 1024
        private var MB = 1024 * KB
        fun prepareTimber(context: Context) {

            val dateTime = LocalDateTime.now()
            val formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

            val dir = "${context.getExternalFilesDir(null)}/ts-logging"
            if(!File(dir).exists()) {
                File(dir).mkdirs()
            }

            Environment.getDataDirectory()
            val t = FileLoggerTree.Builder()
                .withFileName("${formatted}-timedsilence.log")
                .withDirName(dir)
                .withSizeLimit(128*MB)
                .withMinPriority(Log.VERBOSE)
                //.appendToFile(true)
                .withFormatter(formatter)
                .build()
            Forest.plant(t)
        }

        private val formatter = object : Formatter {
            override fun format(priority: Int, tag: String?, message: String): String {
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.GERMAN)
                val time = dateFormatter.format(Date(System.currentTimeMillis()))

                val level = when (priority) {
                    Log.VERBOSE -> "VERB"
                    Log.DEBUG   -> "DEBUG"
                    Log.INFO    -> "INFO"
                    Log.WARN    -> "WARN"
                    Log.ERROR   -> "ERROR"
                    Log.ASSERT  -> "WTF"
                    else        -> "?"
                }

                return String.format(
                    "%-23s %-5s %-20s %s\n",
                    time,
                    level,
                    tag ?: "",
                    message
                )
            }
        }
    }
}