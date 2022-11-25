package spamdetector.zani.adapter

import android.content.Context
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import spamdetector.zani.R
import spamdetector.zani.model.BlockModel
import spamdetector.zani.model.SettingModel
import spamdetector.zani.model.data.BlockInfo
import spamdetector.zani.model.data.ResponseInfo
import spamdetector.zani.util.BlockManager
import spamdetector.zani.util.PreferencesKey
import java.util.*

class BlockListAdapter(private var data : ArrayList<BlockInfo>, private var context: Context):RecyclerView.Adapter<BlockListAdapter.BlockListViewHolder>()
{
    private lateinit var blockManager :BlockManager
    private var settingModel = SettingModel()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockListViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        return BlockListViewHolder(
            layoutInflater,
            parent
        )
    }

    override fun getItemCount(): Int =data.size

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: BlockListViewHolder, position: Int)
    {
        val info : BlockInfo = data[position]
        holder.bind(info)
    }

    fun removeItem(position:Int)
    {
        data.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount-position)
    }


    inner class BlockListViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(
        R.layout.item_specific_block, parent, false))
    {
        private var phoneNumView : TextView? = null
        private var deleteButton : ImageButton? = null

        init {
            phoneNumView = itemView.findViewById(R.id.client_number)
            deleteButton = itemView.findViewById(R.id.delete_block_btn)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                blockManager = BlockManager(context)
            }
        }
        @RequiresApi(Build.VERSION_CODES.N)
        fun bind(info : BlockInfo)
        {
            val pos = adapterPosition
            val formattingNum = PhoneNumberUtils.formatNumber(info.phoneNum, Locale.getDefault().country);
            phoneNumView?.text = formattingNum
            deleteButton?.setOnClickListener{
                val blockModel = BlockModel()
                val callback = { result: ResponseInfo? ->
                    if(settingModel.getBlockPref(PreferencesKey.blockSpecificCallKey))  //지정번호 차단 설정 On
                        blockManager.deleteBlock(phoneNum = data.get(pos).phoneNum)
                    removeItem(pos)
                }
                blockModel.requestDeleteBlock(callback, info.phoneNum)
            }
        }
    }
}