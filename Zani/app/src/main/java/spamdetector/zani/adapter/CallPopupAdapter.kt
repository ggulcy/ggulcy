package spamdetector.zani.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import spamdetector.zani.R
import spamdetector.zani.model.data.CallTypeEnum
import spamdetector.zani.model.data.LogDelimiterEnum
import spamdetector.zani.model.data.LogInfo
import spamdetector.zani.util.UtilManager

class CallPopupAdapter(private var data : List<LogInfo>)
    : RecyclerView.Adapter<CallPopupAdapter.CallPopupViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallPopupViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CallPopupViewHolder(
            layoutInflater,
            parent
        )
    }

    override fun getItemCount(): Int =data.size

    override fun onBindViewHolder(holder: CallPopupViewHolder, position: Int)
    {
        val info : LogInfo = data[position]
        holder.bind(info)
    }

    class CallPopupViewHolder(inflater: LayoutInflater, private var parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(
        R.layout.item_recent_calls_popup, parent, false))
    {
        private var layout: LinearLayout? = null
        private var categoryView: ImageView? = null
        private var clientNameView : TextView? = null
        private var memberNameView : TextView? = null
        private var dateView :TextView? = null
        private var timeView :TextView? = null

        init {
            layout = itemView.findViewById(R.id.call_popup_layout)
            categoryView = itemView.findViewById(R.id.category_view)
            clientNameView = itemView.findViewById(R.id.client_name)
            memberNameView = itemView.findViewById(R.id.member_name)
            dateView = itemView.findViewById(R.id.calling_date)
            timeView = itemView.findViewById(R.id.calling_time)
        }
        fun bind(info : LogInfo)
        {
            categoryView?.setImageResource(info.memberCategory)

            if(info.memberName.isEmpty())
            {
                memberNameView?.visibility = View.GONE
            }
            else
                memberNameView?.text = info.memberName
            dateView?.text = info.date
            timeView?.text = info.time

            if(UtilManager.checkToday("${info.date} ${info.time}"))
            {
                itemView.setBackgroundColor(Color.parseColor("#F4F5F5"))
                if(info.delimiter==LogDelimiterEnum.전화기록)
                {
                    clientNameView?.text = "[오늘 전화 문의]"
                    clientNameView?.setTextColor(ContextCompat.getColor(parent.context, R.color.TodayCallQuestion))
                    when(info.callType)
                    {
                        CallTypeEnum.발신-> categoryView?.setImageResource(R.drawable.call_type_outgoing)
                        CallTypeEnum.부재 -> categoryView?.setImageResource(R.drawable.call_type_missed)
                        CallTypeEnum.수신 -> categoryView?.setImageResource(R.drawable.call_type_income)
                        CallTypeEnum.거절 -> categoryView?.setImageResource(R.drawable.call_type_reject)
                        else -> categoryView?.setImageResource(info.memberCategory)
                    }
                }
                if(info.delimiter==LogDelimiterEnum.문자기록)
                {
                    clientNameView?.text = "[오늘 문자 문의]"
                    clientNameView?.setTextColor(ContextCompat.getColor(parent.context, R.color.TodayMessageQuestion))
                    when(info.callType)
                    {
                        CallTypeEnum.발신-> categoryView?.setImageResource(R.drawable.call_type_outgoing)
                        CallTypeEnum.수신 -> categoryView?.setImageResource(R.drawable.call_type_income)
                        else -> categoryView?.setImageResource(info.memberCategory)
                    }
                }
                if(info.delimiter==LogDelimiterEnum.저장기록)
                {
                    layout?.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorBrightGrayBackground))
                    clientNameView?.text = info.savedName
                }
            }
            else
            {
                itemView.setBackgroundColor(Color.parseColor("#FFFFFF"))
                clientNameView?.text = info.savedName
                clientNameView?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorBasicComment))
                UtilManager.setSpannableString(clientNameView!!, parent.context)
            }
        }
    }
}