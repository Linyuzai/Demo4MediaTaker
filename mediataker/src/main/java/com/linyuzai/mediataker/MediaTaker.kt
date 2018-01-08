package com.linyuzai.mediataker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.linyuzai.mediataker.activity.GalleryActivity
import com.linyuzai.mediataker.activity.PreviewActivity
import java.io.File
import kotlin.reflect.KClass
import android.support.v4.content.FileProvider
import android.os.Build
import java.io.IOException
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.content.ContentUris
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.util.Log

/**
 * Created by linyuzai on 2017/10/31.
 * @author linyuzai
 */
class MediaTaker {
    companion object {
        const val TAKE_PHOTO: Int = 0
        const val SELECT_PHOTO: Int = 1
        const val RECORD_VIDEO: Int = 2
        const val SELECT_VIDEO: Int = 3
    }

    private lateinit var builder: Builder
    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    private var imageFile: File? = null
    private var videoFile: File? = null

    class Builder {
        internal var maxCount: Int = 1
        internal var cacheDir: File? = null
        internal var authorities: String = ""
        internal var previewActivity: KClass<*> = PreviewActivity::class
        internal var galleryActivity: KClass<*> = GalleryActivity::class
        internal var filenameGenerator: (() -> String)? = null
        fun maxCount(count: Int): Builder = apply { this.maxCount = count }
        fun cacheDir(dir: String): Builder = apply { this.cacheDir = File(dir) }
        fun cacheDir(dir: File): Builder = apply { this.cacheDir = dir }
        fun authorities(authorities: String): Builder = apply { this.authorities = authorities }
        fun <T : Activity> previewActivity(preview: KClass<T>): Builder = apply { this.previewActivity = preview }
        fun <T : Activity> galleryActivity(gallery: KClass<T>): Builder = apply { this.galleryActivity = gallery }
        fun filenameGenerator(generator: () -> String): Builder = apply { this.filenameGenerator = generator }
        fun create(): MediaTaker = MediaTaker().apply {
            builder = this@Builder
        }
    }

    /**
     *拍照获取图片
     **/
    fun takePhoto(activity: Activity) {
        val outputImage = File(builder.cacheDir, "${builder.filenameGenerator?.invoke() ?: "temp"}.jpg")
        try {
            if (outputImage.exists()) {
                outputImage.delete()
            }
            outputImage.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        imageFile = outputImage
        imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(activity, builder.authorities, outputImage)
        } else {
            Uri.fromFile(outputImage)
        }
        //启动相机程序
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        activity.startActivityForResult(intent, TAKE_PHOTO)
    }

    /**
     * 从相册中获取图片
     * */
    fun selectPicture(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        activity.startActivityForResult(intent, SELECT_PHOTO)
    }

    fun getTokenPhoto(activity: Activity): Bitmap =
            BitmapFactory.decodeStream(activity.contentResolver.openInputStream(imageUri))

    fun getTokenPhoto(): Uri? = imageUri

    fun getSelectedUri(intent: Intent): Uri = intent.data

    fun getFile(activity: Activity, intent: Intent, operation: Int): File? {
        var uri: Uri? = null
        when (operation) {
        //TAKE_PHOTO -> uri = imageUri
        //RECORD_VIDEO -> uri = videoUri
            TAKE_PHOTO -> return imageFile
            RECORD_VIDEO -> return videoFile
            SELECT_PHOTO -> uri = intent.data
            SELECT_VIDEO -> uri = intent.data
        }
        Log.d("getFilePath", uri.toString())
        if (uri == null)
            return null
        return File(UriUtil.getPath(activity, uri))
    }

    fun getSelectedPath(activity: Activity, uri: Uri?): String {
        if (uri == null) return "Uri is null"
        var imagePath = ""
        //val uri = data.data
        Log.d("getSelectedPath:Path", uri.path)
        Log.d("getSelectedPath:String", uri.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(activity, uri)) {
                //如果是document类型的uri，则通过document id处理
                val docId = DocumentsContract.getDocumentId(uri)
                when {
                    "com.android.providers.media.documents" == uri.authority -> {
                        //解析出数字格式的id
                        val id = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                        val selection = "${BaseColumns._ID}=$id"
                        imagePath = getImagePath(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
                    }
                    "com.android.providers.downloads.documents" == uri.authority -> {
                        val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), docId.toLong())
                        imagePath = getImagePath(activity, contentUri, null)
                    }
                    "content".equals(uri.scheme, ignoreCase = true) -> //如果是content类型的uri，则使用普通方式处理
                        imagePath = getImagePath(activity, uri, null)
                    "file".equals(uri.scheme, ignoreCase = true) -> //如果是file类型的uri，直接获取图片路径即可
                        imagePath = uri.path
                }
            } else {
                imagePath = getImagePath(activity, uri, null)
            }
        } else {
            imagePath = getImagePath(activity, uri, null)
        }
        Log.d("getSelectedPath:Final", imagePath)
        return imagePath
    }

    /**
     * 通过uri和selection来获取真实的图片路径
     */
    private fun getImagePath(context: Context, uri: Uri, selection: String?): String {
        var path = ""
        val cursor = context.contentResolver.query(uri, null, selection, null, null)
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        }
        cursor.close()
        return path
    }

    fun recordVideo(activity: Activity) {
        val outputVideo = File(builder.cacheDir, "${builder.filenameGenerator?.invoke() ?: "temp"}.mp4")
        try {
            if (outputVideo.exists()) {
                outputVideo.delete()
            }
            outputVideo.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        videoFile = outputVideo
        videoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(activity, builder.authorities, outputVideo)
        } else {
            Uri.fromFile(outputVideo)
        }
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        activity.startActivityForResult(intent, RECORD_VIDEO)
    }

    fun getRecordVideo(): Uri? = videoUri

    /**
     * 从相册中获取图片
     * */
    fun selectVideo(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        activity.startActivityForResult(intent, SELECT_VIDEO)
    }
}