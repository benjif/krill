package com.example.krill

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager

fun getDefaultBrowser(context: Context): String? {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
    val packageManager = context.packageManager
    val resolveInfo = packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
    resolveInfo ?: return null
    return resolveInfo.activityInfo.packageName
}

fun getCustomTabsPackages(context: Context): List<String> {
    val packageManager = context.getPackageManager()
    val activityIntent =
        Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
    val resolvedActivityList =
        packageManager.queryIntentActivities(activityIntent, 0)
    val packagesSupportingCustomTabs = mutableListOf<String>()
    resolvedActivityList.forEach {
        val serviceIntent = Intent()
        serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
        serviceIntent.setPackage(it.activityInfo.packageName)
        if (packageManager.resolveService(serviceIntent, 0) != null) {
            packagesSupportingCustomTabs.add(it.activityInfo.packageName)
        }
    }
    return packagesSupportingCustomTabs
}

fun getPreferredCustomTabsPackage(context: Context): String? {
    val defaultBrowser = getDefaultBrowser(context)
    val supportedPackages = getCustomTabsPackages(context)
    if (supportedPackages.isEmpty()) return null
    val preferredPackage =  getCustomTabsPackages(context)
        .firstOrNull { it == defaultBrowser }
    return preferredPackage ?: supportedPackages[0]
}

// Fallback to WebView
// (if user doesn't have a browser that supports custom tabs)
fun openWebview(context: Context, url: String) {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val externalBrowser = sharedPref.getBoolean("externalBrowser", false)
    if (externalBrowser) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        context.startActivity(intent)
    } else {
        val intent = Intent(context, WebActivity::class.java)
        intent.putExtra("link", url)
        context.startActivity(intent)
    }
}

fun openCustomTab(context: Context, url: String) {
    val customTabsBuilder = CustomTabsIntent.Builder()
    customTabsBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
    val customTabsIntent= customTabsBuilder.build()
    val preferredPackage = getPreferredCustomTabsPackage(context)
    if (preferredPackage == null) {
        openWebview(context, url)
        return
    }
    customTabsIntent.intent.setPackage(preferredPackage)
    customTabsIntent.launchUrl(context, Uri.parse(url))
}