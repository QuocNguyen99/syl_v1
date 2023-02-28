package com.hqnguyen.syl.base

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat

object PermissionHelper {
    val locationPermission
        get() = arrayOf(
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION,
            POST_NOTIFICATIONS
        )

    var permissionListDenied = arrayListOf<String>()
    var permissionListNotGranted = arrayListOf<String>()

    fun permissionControl(
        activity: Activity,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit = { showDialogPermission(activity, locationPermission.toList()) },
        vararg permissions: String = locationPermission
    ) {
        permissionsStatus(activity, *permissions)
        if (permissionListNotGranted.size != 0) {
            requestPermission(activity, *permissions)
            return
        }
        if (permissionListDenied.size != 0) {
            onDenied.invoke(permissionListNotGranted)
            return
        }
        onGranted.invoke()
    }

    private fun permissionsStatus(activity: Activity, vararg permissions: String) {
        permissionListDenied.clear()
        permissionListNotGranted.clear()
        for (permission in permissions) {
            when {
                isGranted(activity, permission) -> {
                    continue
                }
                shouldShowRequestPermissionRationale(activity, permission) -> {
                    permissionListDenied.add(permission)
                }
                else -> {
                    permissionListNotGranted.add(permission)
                }
            }
        }
    }

    private fun requestPermission(activity: Activity, vararg permissions: String) {
        ActivityCompat.requestPermissions(
            activity, permissions.asList().toTypedArray(), 101
        )
    }

    fun isGranted(activity: Activity, vararg permissions: String): Boolean {
        permissions.iterator().forEach {
            if (ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun showDialogPermission(activity: Activity, permissions: List<String>) {
        val sb = StringBuilder()
        permissions.iterator().forEach { permission ->
            val s = permission.replace("android.permission.", "")
                .replace("_", " ")
            sb.append(" $s,")
        }
        sb.deleteCharAt(sb.lastIndex)
        val permissionsText = sb.toString()
        AlertDialog.Builder(activity)
            .setTitle("Permission Request")
            .setMessage("Permission:$permissionsText had been denied.\n" +
                    "We need permission for display map.")
            .setPositiveButton("Close") { dialog, _ -> dialog.cancel() }
            .setNegativeButton("Setting") { dialog, _ ->
                dialog.cancel()
                activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    it.data = Uri.fromParts("package", activity.packageName, null)
                })
            }.show()
    }
}