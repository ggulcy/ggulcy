package spamdetector.zani

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_block.*
import spamdetector.zani.adapter.PagerFragmentStateAdapter
import spamdetector.zani.presenter.BlockPresenter
import spamdetector.zani.contract.BlockContract
import spamdetector.zani.fragment.BlockListFragment
import spamdetector.zani.fragment.BlockRecordFragment
import spamdetector.zani.fragment.BlockSettingFragment

class BlockActivity : BaseActivity(), BlockContract.View {
    private lateinit var blockPresenter: BlockPresenter

    private var listFragment: BlockListFragment = BlockListFragment()
    private var settingFragment: BlockSettingFragment= BlockSettingFragment() //콜폭관리
    private var recordFragment: BlockRecordFragment = BlockRecordFragment()    //차단관리

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, )

        initPresenter()
        initView()
        val array = ArrayList<Fragment>()
        array.add(settingFragment)
        array.add(listFragment)

        val pagerAdapter = PagerFragmentStateAdapter(this, array)
        fragment_container_view.adapter = pagerAdapter

        TabLayoutMediator(tab_layout, fragment_container_view) { tab, position ->
            when(position)
            {
                0 -> tab.text = "콜폭 방어"
                1 -> tab.text = "차단 관리"
            }
        }.attach()

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
                return
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                return
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.position==0)
                    menu_name.text = "콜폭 방어"
                else
                    menu_name.text = "차단 관리"
            }

        })

        back_btn.setOnClickListener {
            finish()
        }
    }

    override fun initPresenter() {
        blockPresenter = BlockPresenter()
    }

    override fun initView() {
        blockPresenter.takeView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        blockPresenter.dropView()
    }
}