package spamdetector.zani.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.dialog_insert_block.*
import kotlinx.android.synthetic.main.dialog_insert_block.number_input
import spamdetector.zani.R
import spamdetector.zani.model.BlockModel
import spamdetector.zani.model.data.ResponseInfo
import java.lang.ref.WeakReference

class InsertBlockDialog :Activity() {

    private lateinit var blockManager: BlockManager
    private lateinit var phoneNum : String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Preferences.init(applicationContext)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_insert_block)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            blockManager = BlockManager(applicationContext)
        }

        val blockModel = BlockModel()

        number_input.addTextChangedListener(
            InsertBlockDialog.UsPhoneNumberFormatter(
                WeakReference<EditText>(
                    number_input
                )
            )
        )
        number_input.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(number_input.windowToken, 0)

                phoneNum = number_input.text.toString().replace(")", "").replace("-", "")
                val replaced = phoneNum.replace("-", "")
                val callback = { result: ResponseInfo? ->
                    blockManager.insertBlock(replaced)
                    if(result?.code==200)
                    {
                        Toast.makeText(applicationContext, "차단 번호가 추가되었습니다", Toast.LENGTH_LONG).show();
                    }
                }
                blockModel.requestInsertBlock(callback, replaced)
                true;
            }
            false;
        }
        close_btn.setOnClickListener{
            finish()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {   //바깥레이어 클릭시 안닫히게
        if(event?.action==MotionEvent.ACTION_OUTSIDE)
            return false
        return true
    }

    override fun onBackPressed() {
        finish()
    }
    private class UsPhoneNumberFormatter(private val mWeakEditText: WeakReference<EditText>) :
        TextWatcher {
        //This TextWatcher sub-class formats entered numbers as 1 (123) 456-7890
        private var mFormatting // this is a flag which prevents the
                = false

        // stack(onTextChanged)
        private var clearFlag = false
        private var mLastStartLocation = 0
        private var mLastBeforeText: String? = null
        override fun beforeTextChanged(
            s: CharSequence, start: Int, count: Int,
            after: Int
        ) {
            if (after == 0 && s.toString() == "1 ") {
                clearFlag = true
            }
            mLastStartLocation = start
            mLastBeforeText = s.toString()
        }

        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int,
            count: Int
        ) {
            // TODO: Do nothing
        }

        override fun afterTextChanged(s: Editable) {
            // Make sure to ignore calls to afterTextChanged caused by the work
            // done below
            if (!mFormatting) {
                mFormatting = true
                val curPos = mLastStartLocation
                val beforeValue = mLastBeforeText
                val currentValue = s.toString()
                val formattedValue = formatUsNumber(s)
                if (currentValue.length > beforeValue!!.length) {
                    val setCusorPos = (formattedValue.length
                            - (beforeValue.length - curPos))
                    mWeakEditText.get()!!.setSelection(if (setCusorPos < 0) 0 else setCusorPos)
                } else {
                    var setCusorPos = (formattedValue.length
                            - (currentValue.length - curPos))
                    if (setCusorPos > 0 && !Character.isDigit(formattedValue[setCusorPos - 1])) {
                        setCusorPos--
                    }
                    mWeakEditText.get()!!.setSelection(if (setCusorPos < 0) 0 else setCusorPos)
                }
                mFormatting = false
            }
        }

        private fun formatUsNumber(text: Editable): String {
            val formattedString = StringBuilder()
            // Remove everything except digits
            var p = 0
            while (p < text.length) {
                val ch = text[p]
                if (!Character.isDigit(ch)) {
                    text.delete(p, p + 1)
                } else {
                    p++
                }
            }
            // Now only digits are remaining
            val allDigitString = text.toString()
            val totalDigitCount = allDigitString.length
            if (totalDigitCount == 0 || totalDigitCount > 12 && !allDigitString.startsWith("1")
                || totalDigitCount > 13
            ) {
                // May be the total length of input length is greater than the
                // expected value so we'll remove all formatting
                text.clear()
                text.append(allDigitString)
                return allDigitString
            }
            var alreadyPlacedDigitCount = 0
            // Only '1' is remaining and user pressed backspace and so we clear
            // the edit text.
            if (allDigitString == "1" && clearFlag) {
                text.clear()
                clearFlag = false
                return ""
            }
            if (allDigitString.startsWith("1")) {
                formattedString.append("1 ")
                alreadyPlacedDigitCount++
            }
            // The first 3 numbers beyond '1' must be enclosed in brackets "()"
            if (totalDigitCount - alreadyPlacedDigitCount > 3) {
                formattedString.append(allDigitString.substring(
                    alreadyPlacedDigitCount,
                    alreadyPlacedDigitCount + 3
                ) + ")"
                )
                alreadyPlacedDigitCount += 3
            }
            // There must be a '-' inserted after the next 3 numbers
            if (totalDigitCount - alreadyPlacedDigitCount > 4) {
                formattedString.append(
                    (allDigitString.substring(
                        alreadyPlacedDigitCount, alreadyPlacedDigitCount + 4
                    )
                            + "-")
                )
                alreadyPlacedDigitCount += 4
            }
            // All the required formatting is done so we'll just copy the
            // remaining digits.
            if (totalDigitCount > alreadyPlacedDigitCount) {
                formattedString.append(
                    allDigitString
                        .substring(alreadyPlacedDigitCount)
                )
            }
            text.clear()
            text.append(formattedString.toString())
            return formattedString.toString()
        }
    }
}