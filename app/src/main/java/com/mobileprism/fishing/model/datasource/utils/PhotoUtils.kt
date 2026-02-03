
import android.provider.MediaStore

import android.provider.DocumentsContract
import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.loader.content.CursorLoader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


@SuppressLint("ObsoleteSdkInt")
@Deprecated("Use fileFromContentUri instead", replaceWith = ReplaceWith("fileFromContentUri(context, uri)"))
fun getPathFromURI(context: Context, uri: Uri): String {
    var realPath = ""
    // SDK < API11
    if (Build.VERSION.SDK_INT < 11) {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        @SuppressLint("Recycle") val cursor: Cursor? =
            context.contentResolver.query(uri, proj, null, null, null)
        var column_index = 0
        val result = ""
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            realPath = cursor.getString(column_index)
        }
    } else if (Build.VERSION.SDK_INT < 19) {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursorLoader = CursorLoader(context, uri, proj, null, null, null)
        val cursor: Cursor? = cursorLoader.loadInBackground()
        if (cursor != null) {
            val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            realPath = cursor.getString(column_index)
        }
    } else {
        val wholeID = DocumentsContract.getDocumentId(uri)
        // Split at colon, use second item in the array
        val id = wholeID.split(":").toTypedArray()[1]
        val column = arrayOf(MediaStore.Images.Media.DATA)
        // where id is equal to
        val sel = MediaStore.Images.Media._ID + "=?"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column,
            sel,
            arrayOf(id),
            null
        )
        var columnIndex = 0
        if (cursor != null) {
            columnIndex = cursor.getColumnIndex(column[0])
            if (cursor.moveToFirst()) {
                realPath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
    }
    return realPath
}


fun fileFromContentUri(context: Context, contentUri: Uri): File {
    // Preparing Temp file name
    val fileExtension = getFileExtension(context, contentUri)
    val fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""

    // Creating Temp file
    val tempFile = File(context.cacheDir, fileName)
    tempFile.createNewFile()

    try {
        val oStream = FileOutputStream(tempFile)
        val inputStream = context.contentResolver.openInputStream(contentUri)

        inputStream?.let {
            copy(inputStream, oStream)
        }

        oStream.flush()
    } catch (e: Exception) {
        com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e)
    }

    return tempFile
}

private fun getFileExtension(context: Context, uri: Uri): String? {
    val fileType: String? = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
}

@Throws(IOException::class)
private fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}