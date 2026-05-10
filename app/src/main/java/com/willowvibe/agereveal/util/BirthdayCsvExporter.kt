package com.willowvibe.agereveal.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import com.willowvibe.agereveal.data.model.SavedBirthday
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports the user's saved birthdays as a CSV file via the Android share sheet.
 *
 * Columns: name, birth_date (YYYY-MM-DD), birth_time (HH:mm or empty), emoji, notify
 * The file is stored in cache and exposed via the app's FileProvider.
 */
@Singleton
class BirthdayCsvExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun export(birthdays: List<SavedBirthday>) {
        val csv = buildString {
            append("name,birth_date,birth_time,emoji,notify\n")
            birthdays.forEach { b ->
                val nameSafe = b.name.replace("\"", "\"\"")
                val timeSafe = b.birthTime?.toString() ?: ""
                append("\"$nameSafe\",${b.birthDate},$timeSafe,${b.emoji},${b.notifyEnabled}\n")
            }
        }
        val cacheDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(cacheDir, "cosmicid_birthdays.csv")
        file.writeText(csv)
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Cosmic ID birthdays")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        Handler(Looper.getMainLooper()).post {
            val chooser = Intent.createChooser(intent, "Export birthdays")
            chooser.clipData = ClipData.newRawUri("", uri)
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
