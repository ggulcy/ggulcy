package spamdetector.zani.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import spamdetector.zani.R
import spamdetector.zani.model.data.CallTypeEnum
import spamdetector.zani.model.data.LogDelimiterEnum
import spamdetector.zani.model.data.LogInfo
import spamdetector.zani.util.UtilManager


class ClientLogListAdapter(
    private val context: Context,
    private val arr: ArrayList<LogInfo>
): BaseAdapter() {

    override fun getCount(): Int {
        return arr.size
    }
    override fun getItem(position: Int): Any {
        return arr[position]
    }
    override fun getItemId(position: Int): Long {
        return 0L
    }
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = View.inflate(context, R.layout.item_client_call, null)
        var memberCategory: ImageView = view.findViewById(R.id.category_view)
        var memberName: TextView = view.findViewById(R.id.member_name_view)
        var savedName: TextView = view.findViewById(R.id.saved_name_view)
        var date: TextView = view.findViewById(R.id.calling_date)
        var time: TextView = view.findViewById(R.id.calling_time)

        memberCategory.setImageResource(arr[position].memberCategory)
        memberName.text = arr[position].memberName
        if(arr[position].delimiter==LogDelimiterEnum.저장기록)
        {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBrightGrayBackground))
            savedName.text = arr[position].savedName
            UtilManager.setSpannableString(savedName, context)
        }
        if(UtilManager.checkToday("${arr[position].date} ${arr[position].time}"))     //전화
        {
            if(arr[position].delimiter==LogDelimiterEnum.전화기록)
            {
                savedName.text = "[오늘 전화 문의]"
                savedName.setTextColor(ContextCompat.getColor(context, R.color.TodayCallQuestion))
                when(arr[position].callType)
                {
                    CallTypeEnum.발신-> memberCategory?.setImageResource(R.drawable.call_type_outgoing)
                    CallTypeEnum.부재 -> memberCategory?.setImageResource(R.drawable.call_type_missed)
                    CallTypeEnum.수신 -> memberCategory?.setImageResource(R.drawable.call_type_income)
                    CallTypeEnum.거절 -> memberCategory?.setImageResource(R.drawable.call_type_reject)
                    else -> memberCategory?.setImageResource(arr[position].memberCategory)
                }
            }
            if(arr[position].delimiter==LogDelimiterEnum.문자기록)
            {
                savedName.text = "[오늘 문자 문의]"
                savedName.setTextColor(ContextCompat.getColor(context, R.color.TodayMessageQuestion))
                when(arr[position].callType)
                {
                    CallTypeEnum.발신-> memberCategory?.setImageResource(R.drawable.call_type_outgoing)
                    CallTypeEnum.수신 -> memberCategory?.setImageResource(R.drawable.call_type_income)
                    else -> memberCategory?.setImageResource(arr[position].memberCategory)
                }
            }
        }

        date.text = arr[position].date
        time.text = arr[position].time

        return view
    }
}