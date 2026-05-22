package com.willowvibe.agereveal.notification

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.willowvibe.agereveal.domain.StickerGenerator
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.io.FileNotFoundException

/**
 * ContentProvider that serves cosmic-themed stickers to WhatsApp via its Sticker Pack API.
 *
 * WhatsApp queries this provider after the user taps "Add to WhatsApp" from within Cosmic ID.
 * Stickers are generated on first access and cached to internal storage as 512×512 PNG files.
 */
class WhatsAppStickerProvider : ContentProvider() {

    private lateinit var stickerGenerator: StickerGenerator

    /** Hilt entry point for injecting domain dependencies into the ContentProvider. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StickerProviderEntryPoint {
        fun stickerGenerator(): StickerGenerator
    }

    companion object {
        const val AUTHORITY = "com.willowvibe.cosmicid.stickercontentprovider"
        private const val PACK_IDENTIFIER = "cosmic_id_stickers_v1"
        private const val PACK_NAME = "Cosmic ID Stickers"
        private const val PUBLISHER = "WillowVibe"
        private const val TOTAL_STICKERS = 12
    }

    override fun onCreate(): Boolean {
        val entryPoint = EntryPoints.get(
            requireContext().applicationContext,
            StickerProviderEntryPoint::class.java,
        )
        stickerGenerator = entryPoint.stickerGenerator()
        // Pre-generate stickers on first access so the pack is ready when WhatsApp queries
        stickerGenerator.ensureStickersGenerated()
        stickerGenerator.generateTrayIcon()
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return when {
            uri.path?.contains("metadata") == true -> metadataCursor()
            uri.path?.contains("stickers") == true -> stickersCursor()
            else -> null
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val id = uri.lastPathSegment ?: return null
        val file: File? = when {
            uri.path?.contains("tray") == true -> stickerGenerator.getTrayIconFile()
            id.endsWith(".png") -> stickerGenerator.getStickerFile(id.removeSuffix(".png"))
            else -> stickerGenerator.getStickerFile(id)
        }
        return try {
            ParcelFileDescriptor.open(file ?: return null, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    override fun getType(uri: Uri): String = when {
        uri.path?.endsWith(".png") == true -> "image/png"
        uri.lastPathSegment == "metadata" -> "application/json"
        else -> "image/png"
    }

    private fun metadataCursor(): Cursor {
        return MatrixCursor(arrayOf(
            "identifier", "name", "publisher", "tray_image_file",
            "android_play_store_link", "total_stickers",
            "ios_app_store_link", "publisher_email",
            "publisher_website", "privacy_policy_website",
            "license_agreement_website", "image_data_version",
            "avoid_cache",
        )).apply {
            addRow(arrayOf(
                PACK_IDENTIFIER,
                PACK_NAME,
                PUBLISHER,
                "tray_icon.png",
                "https://play.google.com/store/apps/details?id=com.willowvibe.cosmicid",
                TOTAL_STICKERS.toString(),
                "",
                "hello@willowvibe.com",
                "https://willowvibe.com",
                "https://willowvibe.com/agereveal/privacy",
                "",
                "1",
                "false",
            ))
        }
    }

    private fun stickersCursor(): Cursor {
        return MatrixCursor(arrayOf(
            "identifier", "image_file", "emojis", "size", "is_animated",
        )).apply {
            stickerGenerator.stickers.forEach { sticker ->
                addRow(arrayOf(
                    sticker.id,
                    "${sticker.id}.png",
                    sticker.emojis.joinToString(","),
                    TOTAL_STICKERS.toString(),
                    "false",
                ))
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?) = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0
}
