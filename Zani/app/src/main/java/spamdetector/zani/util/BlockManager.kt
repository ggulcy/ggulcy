package spamdetector.zani.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract.BlockedNumbers
import androidx.annotation.RequiresApi
import spamdetector.zani.model.data.BlockInfo

@RequiresApi(Build.VERSION_CODES.N)
class BlockManager(val context: Context)
{

    fun insertBlock(phoneNum:String)
    {
        val values = ContentValues()
        values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, phoneNum)

        val uri: Uri? = context.getContentResolver().insert(BlockedNumbers.CONTENT_URI, values)
    }

    fun deleteBlock(phoneNum:String)
    {
        val values = ContentValues()
        values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, phoneNum)
        val uri: Uri? = context.getContentResolver().insert(BlockedNumbers.CONTENT_URI, values)
        context.getContentResolver().delete(uri!!, null, null)
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun searchBlock(phoneNumber:String) : Boolean
    {
        val c: Cursor? = context.getContentResolver().query(
            BlockedNumbers.CONTENT_URI, arrayOf(
                BlockedNumbers.COLUMN_ID,
                BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                BlockedNumbers.COLUMN_E164_NUMBER,
            ), null, null, null
        )

        val id = c?.getColumnIndex(BlockedNumbers.COLUMN_ID)
        val number = c?.getColumnIndex(BlockedNumbers.COLUMN_E164_NUMBER)
        val numberOrigin = c?.getColumnIndex(BlockedNumbers.COLUMN_ORIGINAL_NUMBER)

        while(c!!.moveToNext())
        {
            var id = c.getString(id!!)
            var phoneOrigin = c.getString(numberOrigin!!)

            if(phoneNumber==phoneOrigin)
                return true
        }
        return false
    }

    fun getBlockList() : ArrayList<BlockInfo>
    {
        val c: Cursor? = context.getContentResolver().query(
            BlockedNumbers.CONTENT_URI, arrayOf(
                BlockedNumbers.COLUMN_ID,
                BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
            ), null, null, null
        )

        val array = ArrayList<BlockInfo>()

        val id = c?.getColumnIndex(BlockedNumbers.COLUMN_ID)
        val numberOrigin = c?.getColumnIndex(BlockedNumbers.COLUMN_ORIGINAL_NUMBER)

        while(c!!.moveToNext())
        {
            var id = c.getString(id!!)
            var phoneOrigin = c.getString(numberOrigin!!)

            array.add(BlockInfo(phoneNum = phoneOrigin))
        }
        return array
    }
}