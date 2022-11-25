package spamdetector.zani.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import spamdetector.zani.NoticeDetailActivity
import spamdetector.zani.R
import spamdetector.zani.model.data.NoticeInfo

class NoticeListAdapter(private val context: Context, private val arr: ArrayList<NoticeInfo>): BaseAdapter() {

    override fun getCount(): Int {
        return arr.size
    }

    override fun getItem(position: Int): Any {
        return arr[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = View.inflate(context, R.layout.item_notice, null);
        var item: View = view.findViewById(R.id.notice_item)
        var title: TextView = view.findViewById(R.id.notice_title)
        var date: TextView = view.findViewById(R.id.notice_date)
        var content: TextView = view.findViewById(R.id.notice_preview)

        title.text = arr[position].title
        date.text = arr[position].date
        content.text = arr[position].content
        if (arr[position].content.length > 20)
            content.text = arr[position].content.substring(0, 20) + "..."
        item.setOnClickListener {
            var intent = Intent(context, NoticeDetailActivity::class.java)
            intent.putExtra(NoticeDetailActivity.TITLE, arr[position].title)
            intent.putExtra(NoticeDetailActivity.DATE, arr[position].date)
            intent.putExtra(NoticeDetailActivity.CONTENT, arr[position].content)
            context.startActivity(intent)
        }

        return view;
    }
}