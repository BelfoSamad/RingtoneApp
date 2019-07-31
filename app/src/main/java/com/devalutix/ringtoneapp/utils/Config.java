package com.devalutix.ringtoneapp.utils;


/**********************************
 © 2018 Sam Dev
 ALL RIGHTS RESERVED
 ***********************************/
public class Config {

    /**
     * Set this Boolean true if you want to enable Ad Banners or false
     * To disable Ad Banners
     */
    public static final boolean ENABLE_AD_BANNER = false;
    /**
     * Set this Boolean true if you want to enable Interstitial ad or false
     * To disable Interstitial ad
     */
    public static final boolean ENABLE_AD_INTERSTITIAL = true;
    /**
     * Set this Boolean true if you want to enable GDPR or false
     * To disable GDPR, You can disable GDPR if the app isn't targeting a EU Country
     *
     * If You Set this boolean as true, you need to select the ad technology providers used
     * from your admob account
     * Go to Admob > Blocking Control "in the side menu" > Choose EU User Consent
     * then choose "Custom set of ad technology providers"
     */
    public static final boolean ENABLE_GDPR = false;

    /**
     * Add Your Email Here, That email is the email that will receive
     * the Messages from the app users
     */
    public static final String YOUR_EMAIL = "";
    /**
     * Add Your Publisher Id, You can find it on your settings at your admob account
     */
    public static final String PUBLISHER_ID = "pub-4679171106713552";

    /**
     * Facebook Page
     */
    public static final String FACEBOOK_PAGE = "https://www.facebook.com/FacebookAfrica";


    /**
     * Youtube Channel
     */
    public static final String YOUTUBE_CHANNEL = "https://www.youtube.com/user/theofficialfacebook";

    public static final String BASE_API_URL = "https://www.facebook.com/";

    public static final String TOKEN = "test";
}
