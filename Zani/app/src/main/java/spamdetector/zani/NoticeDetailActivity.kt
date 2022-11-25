package spamdetector.zani

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_notice_detail.*

class NoticeDetailActivity : AppCompatActivity() {
    companion object {
        const val TITLE = "TITLE"
        const val CONTENT = "CONTENT"
        const val DATE = "DATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_detail)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, )


        back_btn.setOnClickListener {
            finish()
        }
        notice_title.text = intent.getStringExtra(TITLE)
        notice_date.text = intent.getStringExtra(DATE)
        notice_content.text = intent.getStringExtra(CONTENT)
        notice_content.movementMethod = ScrollingMovementMethod()
    }
    override fun onBackPressed() {
        finish()
    }
}