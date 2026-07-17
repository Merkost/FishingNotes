package com.mobileprism.fishing.ui.home.advertising

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kimplify.cedar.logging.Cedar

object AdsConsentManager {

    private const val CONSENT_TAG = "AdsConsent"

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    fun gatherConsent(
        activity: Activity,
        debugGeographyEea: Boolean,
        onCanRequestAds: () -> Unit,
    ) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder()
            .apply {
                if (debugGeographyEea) {
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                            .build()
                    )
                }
            }
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let { Cedar.tag(CONSENT_TAG).w("Consent form error: ${it.message}") }
                    syncConsentState(consentInformation, onCanRequestAds)
                }
            },
            { requestError ->
                Cedar.tag(CONSENT_TAG).w("Consent info update failed: ${requestError.message}")
                syncConsentState(consentInformation, onCanRequestAds)
            },
        )

        syncConsentState(consentInformation, onCanRequestAds)
    }

    private fun syncConsentState(
        consentInformation: ConsentInformation,
        onCanRequestAds: () -> Unit,
    ) {
        _privacyOptionsRequired.value =
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (consentInformation.canRequestAds() && !_canRequestAds.value) {
            _canRequestAds.value = true
            onCanRequestAds()
        }
    }

    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            formError?.let { Cedar.tag(CONSENT_TAG).w("Privacy options form error: ${it.message}") }
            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            _canRequestAds.value = consentInformation.canRequestAds()
            _privacyOptionsRequired.value =
                consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        }
    }
}
