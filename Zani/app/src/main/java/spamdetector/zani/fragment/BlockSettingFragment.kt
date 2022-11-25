package spamdetector.zani.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import spamdetector.zani.R
import spamdetector.zani.contract.BlockContract
import spamdetector.zani.presenter.BlockSettingPresenter
import spamdetector.zani.util.InsertBlockDialog
import spamdetector.zani.util.Preferences
import spamdetector.zani.util.PreferencesKey

class BlockSettingFragment : Fragment(), BlockContract.RegisterView {

    private lateinit var presenter : BlockSettingPresenter
    private lateinit var unknownSwitch:SwitchCompat
    private lateinit var todaySwitch:SwitchCompat
    private lateinit var specificSwitch:SwitchCompat
    private lateinit var openPopup:TextView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        Preferences.init(requireContext())
        presenter= BlockSettingPresenter(requireContext())
        initView()

        val root = inflater.inflate(R.layout.fragment_block_setting, container, false)
        unknownSwitch = root.findViewById(R.id.block_unknown_switch)
        todaySwitch = root.findViewById(R.id.block_today_switch)
        specificSwitch = root.findViewById(R.id.block_specific_switch)
        openPopup = root.findViewById(R.id.open_popup)

        unknownSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.savePref(PreferencesKey.blockUnknownCallKey, isChecked)
        }
        todaySwitch.setOnCheckedChangeListener{ _, isChecked ->
            presenter.savePref(PreferencesKey.blockTodayCallKey, isChecked)
        }
        specificSwitch.setOnCheckedChangeListener{_, isChecked->
            presenter.savePref(PreferencesKey.blockSpecificCallKey, isChecked)
        }

        openPopup.setOnClickListener {
            startActivity(Intent(requireContext(), InsertBlockDialog::class.java))
        }
        presenter.showPref()

        return root
    }

    override fun showPref(type: String, pref: Boolean) {
        when(type)
        {
            "unknown" -> unknownSwitch.isChecked = pref
            "today" -> todaySwitch.isChecked = pref
            "specific" -> specificSwitch.isChecked = pref
        }
    }

    private fun initView() {
        presenter.takeView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dropView()
    }
}
