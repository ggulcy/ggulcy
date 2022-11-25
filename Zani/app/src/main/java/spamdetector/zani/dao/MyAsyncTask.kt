package spamdetector.zani.dao

import android.os.Handler
import android.os.Looper
import android.os.Message

abstract class MyAsyncTask<T1, T2> : Runnable {

    private var mArgument: T1? = null
    private var mResult: T2? = null
    protected var preCall: (() -> Unit)? = null
    protected var callback: ((T2?) -> Unit)? = null

    // Handle the result
    private val WORK_DONE = 0
    private var mResultHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            // Call onPostExecute
            onPostExecute(mResult)
        }
    }

    fun execute(arg: T1, preCall: (() -> Unit)?, callback: ((T2?) -> Unit)?) {
        mArgument = arg
        this.preCall = preCall
        this.callback = callback

        onPreExecute()
        val thread = Thread(this)
        thread.start()
    }

    override fun run() {
        mResult = doInBackground(mArgument)
        mResultHandler.sendEmptyMessage(WORK_DONE)
    }

    protected abstract fun onPreExecute()
    protected abstract fun doInBackground(arg: T1?): T2?
    protected abstract fun onPostExecute(result: T2?)
}