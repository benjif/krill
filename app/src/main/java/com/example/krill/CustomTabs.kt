package com.example.krill

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

const val ACTION_CUSTOM_TABS_CONNECTION =
    "android.support.customtabs.action.CustomTabsService"

fun getCustomTabsPackages(context: Context): List<ResolveInfo> {
    val packageManager = context.getPackageManager()
    val activityIntent =
        Intent(Intent.ACTION_VIEW, Uri.parse(""))

    val resolvedActivityList =
        packageManager.queryIntentActivities(activityIntent, 0)
    val packagesSupportingCustomTabs = mutableListOf<ResolveInfo>()
    resolvedActivityList.forEach {
        val serviceIntent = Intent()
        serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
        serviceIntent.setPackage(it.activityInfo.packageName)
        if (packageManager.resolveService(serviceIntent, 0) != null) {
            packagesSupportingCustomTabs.add(it)
        }
    }

    return packagesSupportingCustomTabs
}

fun getPreferredCustomTabsPackage(context: Context): ResolveInfo? {
    val supportedPackages = getCustomTabsPackages(context)
    if (supportedPackages.isEmpty())
        return null

    return {
        var curHighest: ResolveInfo = ResolveInfo()
        supportedPackages.forEach {
            if (it.preferredOrder > curHighest.preferredOrder)
                curHighest = it
        }
        curHighest
    }()
}

fun openCustomTab(context: Context, url: String) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    val preferredPackage = getPreferredCustomTabsPackage(context)
    if (preferredPackage != null)
        customTabsIntent.intent.setPackage(preferredPackage.resolvePackageName)
    customTabsIntent.launchUrl(context, Uri.parse(url))
}