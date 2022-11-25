package spamdetector.zani.adapter

import android.content.Intent
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import spamdetector.zani.MainActivity
import spamdetector.zani.R
import spamdetector.zani.model.data.LogDelimiterEnum
import spamdetector.zani.model.data.LogInfo
import spamdetector.zani.util.UtilManager
import java.util.*

class RecentCallAdapter(private var data : List<LogInfo>)
    : RecyclerView.Adapter<RecentCallAdapter.RecentCallViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentCallAdapter.RecentCallViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecentCallViewHolder(
            layoutInflater,
            parent
        )
    }
    override fun onBindViewHolder(holder: RecentCallAdapter.RecentCallViewHolder, position: Int) {
        val info : LogInfo = data[position]
        holder.bind(info)
    }

    override fun getItemCount(): Int =data.size

    inner class RecentCallViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(
        R.layout.item_recent_call, parent, false))
    {
        private var layout : LinearLayout? = null
        private var phoneNumView : TextView? = null
        private var clientNameView : TextView? = null
        private var dateView : TextView? = null
        private var timeView :TextView? = null
        private var imgView : ImageView? = null

        init {
            layout = itemView.findViewById(R.id.item_call)
            phoneNumView = itemView.findViewById(R.id.recentCallPhoneNum)
            dateView = itemView.findViewById(R.id.recentCallDate)
            clientNameView = itemView.findViewById(R.id.recentCallClientName)
            timeView = itemView.findViewById(R.id.recentCallTime)
            imgView = itemView.findViewById(R.id.delimiter_img)
        }
        fun bind(info: LogInfo) {
            phoneNumView?.text = PhoneNumberUtils.formatNumber(info.callNumber, Locale.getDefault().country)
            clientNameView?.text = info.savedName
            dateView?.text = info.date
            timeView?.text = info.time

            layout?.setOnClickListener {
                (MainActivity.activity as MainActivity).search(phoneNumView?.text.toString())
                (MainActivity.activity as MainActivity).closeBottomPanel()
            }
            when (info.delimiter) {
                LogDelimiterEnum.전화기록 -> {
                    imgView?.setImageResource(R.drawable.phone_call_icon)
                    imgView?.setOnClickListener {
                        parent.context.startActivity(Intent(
                            "android.intent.action.CALL",
                            Uri.parse("tel:${UtilManager.changeToHyphenNumber(phoneNumView?.text.toString())}")
                        ))
                    }
                }
                LogDelimiterEnum.문자기록 -> {
                    imgView?.setImageResource(R.drawable.message_icon)
                    imgView?.setOnClickListener {
                        var i = Intent(Intent.ACTION_SENDTO)
                        i.data = Uri.parse("smsto:${phoneNumView?.text}")
                        parent.context.startActivity(i)
                    }
                }
                else -> imgView?.visibility= View.GONE
            }

            if (UtilManager.checkToday("${info.date} ${info.time}")) {
                layout?.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorGrayBackground))
            }
            else
                layout?.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorBasicBackground))
        }
    }
}