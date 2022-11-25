package spamdetector.zani

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_notice.*
import spamdetector.zani.adapter.NoticeListAdapter
import spamdetector.zani.contract.NoticeContract
import spamdetector.zani.model.data.NoticeInfo
import spamdetector.zani.presenter.NoticePresenter
import spamdetector.zani.util.LoadingDialog

class NoticeActivity : BaseActivity(), NoticeContract.View {
    private lateinit var noticePresenter: NoticeContract.Presenter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        initPresenter()
        initView()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        back_btn.setOnClickListener {
            finish()
        }

        noticePresenter.getNotice()
    }
    override fun onBackPressed() {
       finish()
    }


    override fun initPresenter() {
        noticePresenter = NoticePresenter()
    }
    override fun initView() {
        noticePresenter.takeView(this)
    }


    override fun showLoading() {
        loadingDialog = LoadingDialog("공지사항을 불러올게요")
        loadingDialog.show(supportFragmentManager, loadingDialog.tag)
    }
    override fun initList(list: ArrayList<NoticeInfo>) {
        notice_list.adapter = NoticeListAdapter(this, list)
        loadingDialog.dismiss()
    }
}