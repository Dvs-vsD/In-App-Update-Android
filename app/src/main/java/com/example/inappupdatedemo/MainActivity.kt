package com.example.inappupdatedemo

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability


class MainActivity : AppCompatActivity() {

    private var updateManager: AppUpdateManager? = null
    private val REQUEST_CODE: Int = 100

    private val updateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showSnakeBarForCompleteDownload()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvAppVersion = findViewById<TextView>(R.id.app_version_code)
        tvAppVersion.text = BuildConfig.VERSION_CODE.toString()

        initFakeUpdateManager()

        updateManager = AppUpdateManagerFactory.create(this)

        updateManager?.registerListener(updateListener)

    }

    private fun initFakeUpdateManager() {
        val fakeAppUpdateManager = FakeAppUpdateManager(this)
        fakeAppUpdateManager.setUpdateAvailable(5) // add app version code greater than current version.

        fakeAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                updateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    REQUEST_CODE
                )
            }
        }
    }

    /**
     * Check FLEXIBLE app update
     */
    private fun checkForAppUpdate() {
        val appUpdateInfo = updateManager?.appUpdateInfo

        if (appUpdateInfo == null)
            Toast.makeText(this, "Info not available", Toast.LENGTH_SHORT).show()
        else
            Log.d("Update info", "Is Successful: "+ appUpdateInfo.isSuccessful.toString())

        appUpdateInfo?.addOnSuccessListener { updateInfo ->
            if (updateInfo.availableVersionCode() == UpdateAvailability.UPDATE_AVAILABLE
                && updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                updateManager?.startUpdateFlowForResult(
                    updateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    REQUEST_CODE
                )
            } else {
                Toast.makeText(this, "No Update Available !!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSnakeBarForCompleteDownload() {
        Snackbar.make(this, findViewById(R.id.layout), "Update is Ready to Apply", Snackbar.LENGTH_INDEFINITE)
            .setAction("RELOAD") {
                updateManager?.completeUpdate()
            }.show()
    }
}