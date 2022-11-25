package spamdetector.zani.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_block_list.*
import spamdetector.zani.R
import spamdetector.zani.adapter.BlockListAdapter
import spamdetector.zani.contract.BlockContract
import spamdetector.zani.model.data.BlockInfo
import spamdetector.zani.presenter.BlockListPresenter
import spamdetector.zani.util.BlockManager
import spamdetector.zani.util.Preferences
import spamdetector.zani.util.RevDecoration

class BlockListFragment : Fragment(), BlockContract.ListView{

    private lateinit var rev: RecyclerView
    private var presenter = BlockListPresenter()
    private lateinit var blockManager: BlockManager

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Preferences.init(requireContext())
        presenter.takeView(this)

        val root = inflater.inflate(R.layout.fragment_block_list, container, false);
        rev = root.findViewById(R.id.list_container)
        rev.addItemDecoration(RevDecoration(5))
        blockManager = BlockManager(requireContext())
        rev.adapter = BlockListAdapter(ArrayList<BlockInfo>(), requireContext())
        presenter.doGetBlockList()

        return root
    }

    override fun onResume() {
        presenter.doGetBlockList()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dropView()
    }

    override fun showBlockList(array: ArrayList<BlockInfo>) {
        rev.adapter = BlockListAdapter(array, requireContext())
    }

    override fun showBlockMsg(msg: String) {
        block_list_msg.visibility = View.VISIBLE
        block_list_msg.text = msg
        rev.visibility = View.GONE
    }
}