package spamdetector.zani.adapter

import android.os.Build
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import spamdetector.zani.R
import spamdetector.zani.model.data.BlockInfo
import java.util.*

class BlockRecordListAdapter (private var data : ArrayList<BlockInfo>):RecyclerView.Adapter<BlockRecordListAdapter.BlockRecordViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockRecordViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        return BlockRecordViewHolder(
            layoutInflater,
            parent
        )
    }

    override fun getItemCount(): Int =data.size

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: BlockRecordViewHolder, position: Int)
    {
        val info : BlockInfo = data[position]
        holder.bind(info)
    }

    inner class BlockRecordViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(
        R.layout.item_block_record, parent, false))
    {
        private var phoneNumView : TextView? = null
        private var dateView : TextView? = null

        init {
            phoneNumView = itemView.findViewById(R.id.phoneNum)
            dateView = itemView.findViewById(R.id.date)
        }
        @RequiresApi(Build.VERSION_CODES.N)
        fun bind(info : BlockInfo)
        {
            val pos = adapterPosition
            val formattingNum = PhoneNumberUtils.formatNumber(info.phoneNum, Locale.getDefault().country);
            phoneNumView?.text = formattingNum
            dateView?.text = info.date

        }
    }
}