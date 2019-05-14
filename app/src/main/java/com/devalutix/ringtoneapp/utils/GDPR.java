package com.devalutix.ringtoneapp.utils;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.net.MalformedURLException;
import java.net.URL;

public class GDPR {
    private static final String TAG = "GDPR";

    private ConsentForm form;
    private AdView ad;
    private Context mContext;

    public GDPR(ConsentForm form, Context mContext) {
        this.form = form;
        this.mContext = mContext;
    }

    public void setAd(AdView ad) {
        this.ad = ad;
    }

    /**
     * GDPR CODE
     */
    public void checkForConsent() {
        Log.d(TAG, "checkForConsent: Checking For Consent");

        ConsentInformation consentInformation = ConsentInformation.getInstance(mContext);
        String[] publisherIds = {Config.PUBLISHER_ID};

        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d(TAG, "onConsentInfoUpdated: Successful");
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        Log.d(TAG, "Showing Personalized ads");
                        showPersonalizedAds();
                        break;
                    case NON_PERSONALIZED:
                        Log.d(TAG, "Showing Non-Personalized ads");
                        showNonPersonalizedAds();
                        break;
                    case UNKNOWN:
                        Log.d(TAG, "Requesting Consent");
                        if (ConsentInformation.getInstance(mContext)
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent();
                        } else {
                            showPersonalizedAds();
                        }
                        break;
                    default:
                        Log.d(TAG, "onConsentInfoUpdated: Nothing");
                        break;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                Log.d(TAG, "onFailedToUpdateConsentInfo: Failed to update");
                // User's consent status failed to update.
            }
        });
    }

    public void requestConsent() {
        Log.d(TAG, "requestConsent: Requesting Consent");
        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL("your privacy link");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(mContext, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        Log.d(TAG, "Requesting Consent: onConsentFormLoaded");
                        showForm();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                        Log.d(TAG, "Requesting Consent: onConsentFormOpened");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d(TAG, "Requesting Consent: onConsentFormClosed");
                        if (userPrefersAdFree) {
                            // Buy or Subscribe
                            Log.d(TAG, "Requesting Consent: User prefers AdFree");
                        } else {
                            Log.d(TAG, "Requesting Consent: Requesting consent again");
                            switch (consentStatus) {
                                case PERSONALIZED:
                                    showPersonalizedAds();
                                    break;
                                case NON_PERSONALIZED:
                                    showNonPersonalizedAds();
                                    break;
                                case UNKNOWN:
                                    showNonPersonalizedAds();
                                    break;
                            }

                        }
                        // Consent form was closed.
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d(TAG, "Requesting Consent: onConsentFormError. Error - " + errorDescription);
                        // Consent form error.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

    private void showPersonalizedAds() {
        Log.d(TAG, "showPersonalizedAds: Showing Personalized Ad");

        //banner
            Log.d(TAG, "showPersonalizedAds: Setting Intesrtitial Ad");
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            ad.loadAd(adRequest);
    }

    private void showNonPersonalizedAds() {
        Log.d(TAG, "showPersonalizedAds: Showing UnPersonalized Ad");

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle())
                    .build();
            ad.loadAd(adRequest);
    }

    private Bundle getNonPersonalizedAdsBundle() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");

        return extras;
    }

    private void showForm() {
        if (form == null) {
            Log.d(TAG, "Consent form is null");
        }
        if (form != null) {
            Log.d(TAG, "Showing consent form");
            form.show();
        } else {
            Log.d(TAG, "Not Showing consent form");
        }
    }

    public AdView getmAd() {
        return ad;
    }
}
