package spamdetector.zani.util

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import spamdetector.zani.R

class LoadingDialog(private val comment: String) : BottomSheetDialogFragment() {

    private lateinit var commentView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false // block user dismiss

        super.onCreateView(inflater, container, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        var view = inflater.inflate(R.layout.dialog_loading, container, false)
        commentView = view.findViewById(R.id.loading_comment)
        commentView.text = comment
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            // window?.setDimAmount(0.2f) // Set dim amount here
            setOnShowListener {
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)
            }
        }
    }
}