
import android.provider.MediaStore

import android.provider.DocumentsContract
import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.loader.content.CursorLoader


@SuppressLint("ObsoleteSdkInt")
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