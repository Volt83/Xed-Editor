package com.rk.xededitor.Settings

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rk.libPlugin.client.ManagePlugin
import com.rk.libcommons.After
import com.rk.xededitor.BaseActivity
import com.rk.xededitor.LoadingPopup
import com.rk.xededitor.R
import com.rk.xededitor.databinding.ActivitySettingsMainBinding
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.onCheckedChange
import de.Maxr1998.modernpreferences.helpers.onClickView
import de.Maxr1998.modernpreferences.helpers.pref
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.switch

class SettingsPlugins : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivitySettingsMainBinding
    private lateinit var padapter: PreferencesAdapter
    private lateinit var playoutManager: LinearLayoutManager

    fun get_recycler_view(): RecyclerView {
        binding = ActivitySettingsMainBinding.inflate(layoutInflater)
        recyclerView = binding.recyclerView
        return recyclerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        padapter = PreferencesAdapter(getScreen())

        savedInstanceState?.getParcelable<PreferencesAdapter.SavedState>("padapter")
            ?.let(padapter::loadSavedState)

        playoutManager = LinearLayoutManager(this)
        get_recycler_view().apply {
            layoutManager = playoutManager
            adapter = padapter
        }

        setContentView(binding.root)
        binding.toolbar.title = "Plugins"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (SettingsData.isDarkMode(this) && SettingsData.isOled(this)) {
            binding.root.setBackgroundColor(Color.BLACK)
            binding.toolbar.setBackgroundColor(Color.BLACK)
            binding.appbar.setBackgroundColor(Color.BLACK)
            window.navigationBarColor = Color.BLACK
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
        } else if (SettingsData.isDarkMode(this)) {
            val window = window
            window.navigationBarColor = Color.parseColor("#141118")
        }

    }

    fun getScreen(): PreferenceScreen {
        return screen(this) {
            switch("plugins") {
                title = "Enable Plugins"
                summary = "Execute active plugins"
                iconRes = R.drawable.extension
                onCheckedChange { isChecked ->
                    SettingsData.setBoolean(this@SettingsPlugins, "enablePlugin", isChecked)
                    LoadingPopup(this@SettingsPlugins, 200)
                    com.rk.libcommons.After(230) {
                        runOnUiThread {
                            this@SettingsPlugins.recreate()
                        }
                    }
                    return@onCheckedChange true
                }
            }
            if (SettingsData.getBoolean(this@SettingsPlugins, "enablePlugin", false)) {
                pref("managePlugin") {
                    title = "Manage Plugins"
                    summary = "on/off installed plugins"
                    iconRes = R.drawable.extension
                    onClickView {
                        startActivity(Intent(this@SettingsPlugins, ManagePlugin::class.java))
                    }
                }
            }
        }


    }
}
