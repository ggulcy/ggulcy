package spamdetector.zani.dao

import android.util.Log
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.URL

class SaveLogTask : MyAsyncTask<ArrayList<String>, String>() {

    override fun onPreExecute() { }

    override fun doInBackground(arg: ArrayList<String>?): String? {
        try {
            var con: HttpURLConnection? = null
            var reader: BufferedReader? = null

            val url = URL(arg?.get(0))
            con = url.openConnection() as HttpURLConnection?

            if (con == null) {
                return null
            }

            con.requestMethod = "POST"
            con.setRequestProperty("Cache-Control", "no-cache") // cache 설정
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "text/json")
            con.doInput = true
            con.connect()

            val outStream: OutputStream = con.outputStream
            val writer: BufferedWriter = BufferedWriter(OutputStreamWriter(outStream))
            writer.write(arg?.get(1))
            writer.flush()
            writer.close()

            val stream: InputStream = con.inputStream
            reader = BufferedReader(InputStreamReader(stream))
            val builder = StringBuilder()
            var line: String? = ""
            while (reader.readLine().also { line = it } != null)
            {
                builder.append(line)
            }
            return JSONObject(builder.toString()).getString("msg")
        } catch (e: SocketException) {
            return "network"
        } catch (e: Exception) {
            return "other"
        }
    }

    override fun onPostExecute(result: String?) {
        callback?.let { it(result) }
    }
}