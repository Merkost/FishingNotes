package com.mobileprism.fishing.ui.home.advertising

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.MainActivity

const val TAG = "FISHING"

fun showInterstitialAd(context: Context, onAdLoaded: () -> Unit) {
    InterstitialAd.load(
        context,
        context.getString(R.string.new_catch_loading_admob_fullscreen_id),
        AdRequest.Builder().build(),
        createInterstitialAdLoadCallback(context = context, onAdLoaded = onAdLoaded)
    )
}

fun createInterstitialAdLoadCallback(
    context: Context,
    onAdLoaded: () -> Unit
): InterstitialAdLoadCallback =
    object : InterstitialAdLoadCallback() {
        var mInterstitialAd: InterstitialAd? = null
        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            Log.d(TAG, "Ad was loaded.")
            mInterstitialAd = interstitialAd
            mInterstitialAd?.show(context as MainActivity)
            onAdLoaded()
            super.onAdLoaded(interstitialAd)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
            Log.d(TAG, adError.message)
            mInterstitialAd = null
            onAdLoaded()
            super.onAdFailedToLoad(adError)
        }
    }