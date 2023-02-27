package com.hqnguyen.syl.base


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.*


class PermissionsStatus {
    val deniedPermissions = mutableListOf<String>()
    val notGrantedPermissions = mutableListOf<String>()
    val shouldShowRequestPermission get() = notGrantedPermissions.isNotEmpty()
    val hasDeniedPermission get() = deniedPermissions.isNotEmpty()
    val isGranted: Boolean get() = !shouldShowRequestPermission && !hasDeniedPermission

    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, notGrantedPermissions.toTypedArray(), 101
        )
    }
}

val locationPermission
    get() = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

fun LifecycleOwner.permissionsStatus(vararg permissions: String): PermissionsStatus {
    val status = PermissionsStatus()
    val activity = when (this) {
        is Fragment -> this.requireActivity()
        is Activity -> this
        else -> null
    } ?: return status
    for (permission in permissions) {
        when {
            isGranted(permission) -> {
                continue
            }
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                status.deniedPermissions.add(permission)
            }
            else -> {
                status.notGrantedPermissions.add(permission)
            }
        }
    }
    return status
}

private fun requestedPermission(): Array<String> {
    return app.packageManager.getPackageInfo(
        app.packageName, PackageManager.GET_PERMISSIONS
    ).requestedPermissions
}

fun isGranted(vararg permissions: String): Boolean {
    permissions.iterator().forEach {
        if (ContextCompat.checkSelfPermission(app, it) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun LifecycleOwner.showDialogPermission(permissions: List<String>) {
    val sb = StringBuilder()
    permissions.iterator().forEach { permission ->
        val s = permission.replace("android.permission.", "")
            .replace("_", " ").lowercase(Locale.getDefault())
        sb.append(" $s,")
    }
    sb.deleteCharAt(sb.lastIndex)
    val permissionsText = sb.toString()
    val activity = requireActivity() ?: return
    AlertDialog.Builder(activity)
        .setMessage("Permission:$permissionsText had been denied")
        .setPositiveButton("Close") { dialog, _ -> dialog.cancel() }
        .setNegativeButton("Setting") { dialog, _ ->
            dialog.cancel()
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.data = Uri.fromParts("package", activity.packageName, null)
            })
        }.show()
}

fun LifecycleOwner.requestPermission(vararg permissions: String) {
    val activity: Activity = this.requireActivity() ?: return
    val status = permissionsStatus(*permissions)
    if (status.notGrantedPermissions.isNotEmpty()) {
        status.requestPermissions(activity)
        return
    }
}

fun LifecycleOwner.observerPermission(
    vararg permissions: String,
    onGranted: () -> Unit,
    onDenied: (List<String>) -> Unit = { showDialogPermission(permissions.toList()) }
) {
    if (isGranted(*permissions)) {
        onGranted.invoke()
        return
    }
    val activity: ComponentActivity = this.requireActivity() as? ComponentActivity ?: return
    val status = permissionsStatus(*permissions)
    if (status.shouldShowRequestPermission) {
        activity.lifecycle.addObserver(object : PermissionObserver(permissions) {
            override fun onResult(owner: LifecycleOwner, status: PermissionsStatus) {
                if (status.isGranted) {
                    onGranted.invoke()
                    owner.lifecycle.removeObserver(self)
                } else {
                    onDenied(status.deniedPermissions)
                }
            }
        })
        status.requestPermissions(activity)
        return
    }
    if (status.hasDeniedPermission) {
        onDenied(status.deniedPermissions)
        return
    }
    onGranted.invoke()
}

fun LifecycleOwner.onGrantedPermission(
    vararg permissions: String,
    onAllow: () -> Unit,
    onDenied: (List<String>) -> Unit = { showDialogPermission(permissions.toList()) }
) {
    val activity: ComponentActivity = this.requireActivity() as? ComponentActivity ?: return
    val status = permissionsStatus(*permissions)
    if (status.shouldShowRequestPermission) {
        activity.lifecycle.addObserver(object : PermissionObserver(permissions) {
            override fun onResult(owner: LifecycleOwner, status: PermissionsStatus) {
                if (status.isGranted) {
                    onAllow.invoke()
                } else {
                    onDenied(status.deniedPermissions)
                }
                owner.lifecycle.removeObserver(self)
            }
        })
        status.requestPermissions(activity)
        return
    }
    if (status.hasDeniedPermission) {
        onDenied(status.deniedPermissions)
        return
    }
    onAllow.invoke()
}

fun LifecycleOwner.listenPermission(
    vararg permissions: String,
    onGranted: () -> Unit,
) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            if (isGranted(*permissions)) {
                onGranted()
            }
        }
    })

}

fun LifecycleOwner.ifGrantedPermission(
    vararg permissions: String,
    onGranted: () -> Unit,
) {
    onGrantedPermission(*permissions, onAllow = onGranted, onDenied = {})
}

abstract class PermissionObserver(val permissions: Array<out String>) : DefaultLifecycleObserver {
    val self get() = this

    private var resultJob: Job? = null

    override fun onPause(owner: LifecycleOwner) {
        resultJob?.cancel()
        resultJob = owner.lifecycleScope.launchWhenResumed {
            if (permissions.size > 1 && Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                // because multi permissions request error on Samsung android 13
                // it return permission was denied when any permission allow only once
                delay(600)
            }
            val newStatus = owner.permissionsStatus(*permissions)
            onResult(owner, newStatus)
        }
    }

    abstract fun onResult(owner: LifecycleOwner, status: PermissionsStatus)

}











