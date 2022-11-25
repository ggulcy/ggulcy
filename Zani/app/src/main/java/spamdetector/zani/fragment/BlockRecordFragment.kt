package spamdetector.zani.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_block_record.*
import spamdetector.zani.R
import spamdetector.zani.adapter.BlockRecordListAdapter
import spamdetector.zani.contract.BlockContract
import spamdetector.zani.model.data.BlockInfo
import spamdetector.zani.presenter.BlockRecordPresenter
import spamdetector.zani.util.Preferences
import spamdetector.zani.util.RevDecoration

class BlockRecordFragment : Fragment(), BlockContract.RecordView  {

    private lateinit var revView : RecyclerView
    private var presenter =  BlockRecordPresenter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        Preferences.init(requireContext())
        presenter.takeView(this)
        val root = inflater.inflate(R.layout.fragment_block_record, container, false);
        revView = root.findViewById(R.id.block_record_rev)
        revView.addItemDecoration(RevDecoration(5))
        revView.adapter = BlockRecordListAdapter(ArrayList<BlockInfo>())
        presenter.doGetRecordList()

        return root
    }

    override fun showRecordList(array:ArrayList<BlockInfo>) {
        revView.adapter = BlockRecordListAdapter(array)
    }

    override fun showRecordMsg(msg:String) {
        block_record_msg.visibility = View.VISIBLE
        block_record_msg.text= msg
        revView.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dropView()
    }
}