package com.harzallah.crazyrun;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Harzallah on 24/10/2015.
 */
public class GlobalState extends Application implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "Crazy Run TAG";
    private static final int REQUEST_ACHIEVEMENTS = 101;
    private static final int REQUEST_LEADERBOARD = 102;
    private static boolean showAd=true;
    private Activity mActivity;



    private int nbLifes,nbDiamonds,mNiveau;
    private boolean mSoundsEnabled = true;
    private int mReachedLevel,mRepeatLevel;

    private GestionFichier gF;
    public static final String CONNECTION = "*CNN:";
    public static final String SOUNDS = "*SND:";
    public static final String NIVEAU = "*NIV:";
    public static final String REACHED_LEVEL = "*RLV:";
    public static final String REPEAT_LEVEL = "*RPL:";
    public static final String LIFES = "*LFS:";
    private static final String IS_DEAD = "*ISD:";
    public static final String DIAMONDS = "*DMS:";
    public static final String OLD_TIME = "*OLD:";
    public static final String TOTAL_SCORE = "*TTS:";
    public static final String mFichier = "1006";
    public static final String mFichierNiveau = "1006N";
    public static final String mFichierScore = "1006S";
    public static final String mAchievementsFile = "1006A";

    private IabHelper mHelper;
    private String base64EncodedPublicKey;
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener;
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener;
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener;
    private final static String SKU_DIAMONDS = "diamonds";
    private final static String SKU_DIAMONDS20 = "diamonds_20";
    private final static String SKU_DIAMONDS50 = "diamonds_50";

    private GoogleApiClient mGoogleApiClient;
    protected static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    protected boolean mSignInClicked = false;
    protected Person currentPerson;
    protected boolean mIsResolving;
    protected boolean mShouldResolve;
    private boolean connectIsEnabled;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;


    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();
    private int isDead;
    private long bestScore;
    private long totalScore;


    private static PublisherInterstitialAd mPublisherInterstitialAd;
    private boolean showDialog=false;

    private Tracker mTracker;


    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        gF = new GestionFichier(getApplicationContext());
        initializationMyFiles();

        //AD
        mPublisherInterstitialAd = new PublisherInterstitialAd(this);
        mPublisherInterstitialAd.setAdUnitId("ca-app-pub-6886979529982097/9051312464");
        requestNewInterstitial();


        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                //VAR
            }
        });

        mSoundsEnabled = Boolean.valueOf(gF.selectValeur(mFichier,SOUNDS));
        connectIsEnabled = Boolean.valueOf(gF.selectValeur(mFichier,CONNECTION));
        nbDiamonds = Integer.valueOf(gF.selectValeur(mFichier, DIAMONDS));
        mNiveau = Integer.valueOf(gF.selectValeur(mFichier, NIVEAU));
        loadBestScore();
        mReachedLevel = Integer.valueOf(gF.selectValeur(mFichier,REACHED_LEVEL));
        mRepeatLevel = Integer.valueOf(gF.selectValeur(mFichier,REPEAT_LEVEL));
        isDead = Integer.valueOf(gF.selectValeur(mFichier,IS_DEAD));
        totalScore = Long.valueOf(gF.selectValeur(mFichier,TOTAL_SCORE));

        // load outbox from file
        mOutbox.loadLocal(mActivity);

        base64EncodedPublicKey = getString(R.string.Base64);
        mHelper = new IabHelper(this, base64EncodedPublicKey);
/*
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.

            }
        });

*/

        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API) .addScope(Plus.SCOPE_PLUS_LOGIN) .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                        // add other APIs and scopes here as needed
                .build();


        // Listener that's called when we finish querying the items and subscriptions we own
        mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Is it a failure?
                if (result.isFailure()) {
                    return;
                }


            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

                // Check for gas delivery -- if we own gas, we should fill up the tank immediately
                Purchase mPurchase = inventory.getPurchase(SKU_DIAMONDS);
                if (mPurchase != null && verifyDeveloperPayload(mPurchase)) {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_DIAMONDS), mConsumeFinishedListener);
                    return;
                }

                mPurchase = inventory.getPurchase(SKU_DIAMONDS20);
                if (mPurchase != null && verifyDeveloperPayload(mPurchase)) {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_DIAMONDS20), mConsumeFinishedListener);
                    return;
                }

                mPurchase = inventory.getPurchase(SKU_DIAMONDS50);
                if (mPurchase != null && verifyDeveloperPayload(mPurchase)) {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_DIAMONDS50), mConsumeFinishedListener);
                    return;
                }

                // update the UI
                String priceR1 = inventory.getSkuDetails(SKU_DIAMONDS).getTitle() +" = "+ inventory.getSkuDetails(SKU_DIAMONDS).getPrice();
                String priceR2 = inventory.getSkuDetails(SKU_DIAMONDS20).getTitle() +" = "+ inventory.getSkuDetails(SKU_DIAMONDS20).getPrice();
                String priceR3 = inventory.getSkuDetails(SKU_DIAMONDS50).getTitle() +" = "+ inventory.getSkuDetails(SKU_DIAMONDS50).getPrice();

                getDiamonds (priceR1,priceR2,priceR3);

            }
        };



        // Callback for when a purchase is finished
        mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

                // if we were disposed of in the meantime, quit.
                if (mHelper == null) return;

                if (result.isFailure()) {
                    return;
                }
                if (!verifyDeveloperPayload(purchase)) {
                    return;
                }


                mHelper.consumeAsync(purchase, mConsumeFinishedListener);



            }
        };



        // Called when consumption is complete
        mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
            public void onConsumeFinished(Purchase purchase, IabResult result) {

                // if we were disposed of in the meantime, quit.
                if (mHelper == null) return;

                // We know this is the "gas" sku because it's the only one we consume,
                // so we don't check which sku was consumed. If you have more than one
                // sku, you probably should check...
                if (result.isSuccess()) {
                    // successfully consumed, so we apply the effects of the item in our
                    // game world's logic, which in our case means filling the gas tank a bit
                    if (purchase.getSku().equals(SKU_DIAMONDS)) {
                        incrementNbDiamonds(10);
                    }
                    else if (purchase.getSku().equals(SKU_DIAMONDS20))
                    {
                        incrementNbDiamonds(20);
                    }
                    else if (purchase.getSku().equals(SKU_DIAMONDS50))
                    {
                        incrementNbDiamonds(50);
                    }
                    /*else if (purchase.getSku().equals("android.test.purchased"))
                    {
                        incrementNbDiamonds(100);
                    }
*/

                    getLifes();

                }
                else {
                }

            }
        };

    }

    public void requestNewInterstitial() {
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder()
                .build();

        mPublisherInterstitialAd.loadAd(adRequest);
    }

    public void myInfoToast (String msg)
    {

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.info_toast,
                (ViewGroup) mActivity.findViewById(R.id.info_toast_layout));

        ImageView image = (ImageView) layout.findViewById(R.id.toast_ic);
        image.setImageResource(R.drawable.panda_accident);
        TextView text = (TextView) layout.findViewById(R.id.msg);
        text.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    public void myAchievementToast (String msg)
    {
        if (!googleApiClientIsConnected()) {

            LayoutInflater inflater = mActivity.getLayoutInflater();
            final View layout = inflater.inflate(R.layout.info_toast,
                    (ViewGroup) mActivity.findViewById(R.id.info_toast_layout));

            ImageView image = (ImageView) layout.findViewById(R.id.toast_ic);
            image.setImageResource(R.drawable.reussites_ic);
            TextView text = (TextView) layout.findViewById(R.id.msg);
            text.setText(getString(R.string.achievement) + msg);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = new Toast(getApplicationContext());
                    toast.setView(layout);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.show();
                }
            });

        }
    }


    public int getmRepeatLevel() {
        return mRepeatLevel;
    }

    public void incrementmRepeatLevel() {
        mRepeatLevel++;
    }

    public void savemRepeatLevel ()
    {
        gF.updateValeur(mFichier,REPEAT_LEVEL,mRepeatLevel+"");

    }

    public void resetmRepeatLevel ()
    {
        mRepeatLevel = 0;

    }

    protected void disableConnection ()
    {
        connectIsEnabled = false;
        gF.updateValeur(mFichier,CONNECTION,"false");
    }

    protected void enableConnection ()
    {
        connectIsEnabled = true;
        gF.updateValeur(mFichier,CONNECTION,"true");
    }

    private void initializationMyFiles() {
        if (!gF.existe(mFichier))
        {
            gF.creeFichier(mFichier);
            gF.updateValeur(mFichier, NIVEAU, "1");
            gF.updateValeur(mFichier, REACHED_LEVEL,"1");
            gF.updateValeur(mFichier,LIFES,"20");
            gF.updateValeur(mFichier,OLD_TIME,"2015!01!10!10!10!10");
            gF.updateValeur(mFichier,DIAMONDS,"20");
            gF.updateValeur(mFichier,CONNECTION,"true");
            gF.updateValeur(mFichier,SOUNDS,"true");
            gF.updateValeur(mFichier,REPEAT_LEVEL,"0");
            gF.updateValeur(mFichier,IS_DEAD,"0");
            gF.updateValeur(mFichier,TOTAL_SCORE,"0");
        }

        if (!gF.existe(mFichierScore))
        {
            gF.creeFichier(mFichierScore);
        }

        if (!gF.existe(mFichierNiveau))
        {
            try {
                //init BDD
                InputStream myInput = this.getAssets().open("1006N.txt");

                //Open the empty db as the output stream
                FileOutputStream fos = openFileOutput(mFichierNiveau, Context.MODE_PRIVATE);

                //transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer))>0){
                    fos.write(buffer, 0, length);
                }

                //Close the streams
                fos.flush();
                fos.close();
                myInput.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public static String getTAG() {
        return TAG;
    }

    public void getLifes ()
    {
        CustomDialogLifes switchMenu=new CustomDialogLifes(mActivity);
        switchMenu.setDialogResult(new CustomDialogLifes.MyDialogListener() {
            @Override
            public void selectedChoise(int choice) {
                if (choice == 4)
                {
                    if ( getNbDiamonds() >= 4) {
                        incrementNbLifes(10);
                        incrementNbDiamonds(-4);
                    } else {

                            purchaseDialog();

                    }
                }
                else if (choice == 8)
                {
                    if (getNbDiamonds() >= 8)
                    {
                        incrementNbLifes(25);
                        incrementNbDiamonds(-8);
                    }
                    else
                    {

                            purchaseDialog();

                    }

                }
            }
        });
        switchMenu.show();
    }

    protected void waitDialog()
    {
        CustomDialogWait purchaseMenu;
        if (nbLifes < 1)
            purchaseMenu = new CustomDialogWait(mActivity,1);
        else
            purchaseMenu = new CustomDialogWait(mActivity,2);

        purchaseMenu.show();
    }

    protected void purchaseDialog() {

        if (isNetworkAvailable()) {

            List additionalSkuList = new ArrayList();
            additionalSkuList.add(SKU_DIAMONDS);
            additionalSkuList.add(SKU_DIAMONDS20);
            additionalSkuList.add(SKU_DIAMONDS50);


            try {
                mHelper.queryInventoryAsync(true, additionalSkuList,
                        mGotInventoryListener);
            } catch (IllegalStateException e) {

            }
        }
        else
        {
            waitDialog();
        }
    }

    private void getDiamonds(String priceR1, String priceR2, String priceR3) {

        CustomDialogDiamonds purchaseMenu = new CustomDialogDiamonds(mActivity, priceR1, priceR2, priceR3);
        purchaseMenu.setDialogResult(new CustomDialogDiamonds.MyDialogListener() {
            @Override
            public void selectedChoise(int choice) {

                if (mHelper.mSetupDone) {

                    String payload = "IZI92PANAME1010";
                    String object;
                    if (choice == 10)
                    {
                        object = SKU_DIAMONDS;
                    }
                    else if (choice == 20)
                    {
                        object = SKU_DIAMONDS20;
                    }
                    else
                    {
                        object = SKU_DIAMONDS50;
                    }
                    mHelper.launchPurchaseFlow(mActivity, object, 10001,
                            mPurchaseFinishedListener, payload);
                }

            }
        });
        purchaseMenu.show();
    }

    public static void showAD ()
    {
        if (mPublisherInterstitialAd.isLoaded() && showAd) {
            mPublisherInterstitialAd.show();
            showAd = false;
        } else {
            showAd = true;
        }
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void setLifes()
    {

        nbLifes = Integer.valueOf(gF.selectValeur(mFichier, LIFES));
        String []tab = gF.selectValeur(mFichier,OLD_TIME).split("!");
        Calendar oldDate = new GregorianCalendar(Integer.valueOf(tab[0]),Integer.valueOf(tab[1]),Integer.valueOf(tab[2]),Integer.valueOf(tab[3]),Integer.valueOf(tab[4]),Integer.valueOf(tab[5]));
        Calendar actDate = Calendar.getInstance();

        long difference = (actDate.getTimeInMillis() - oldDate.getTimeInMillis()) / (1000);
        int lifesWin =(int) (difference / (3 * 60));
        int stillSecondes =(int) (difference % (3 * 60));


        if ( lifesWin >= 1)
        {
            if (lifesWin > 20 - nbLifes)
            {
                nbLifes = 20;
            }
            else
            {
                nbLifes += lifesWin;
            }
            actDate.add(Calendar.SECOND,-stillSecondes);
            gF.updateValeur(mFichier, OLD_TIME, actDate.get(Calendar.YEAR) + "!" + actDate.get(Calendar.MONTH) + "!" + actDate.get(Calendar.DAY_OF_MONTH) + "!" + actDate.get(Calendar.HOUR_OF_DAY) + "!" + (actDate.get(Calendar.MINUTE)) + "!" + (actDate.get(Calendar.SECOND)));

        }
    }
    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public String[] getmNiveauParams() {
        return (gF.selectValeur(mFichierNiveau,"*NIV"+mNiveau+":").split(";"));
    }

    public int getNiveau() {
        return mNiveau;
    }

    public int getNbLifes() {
        return nbLifes;
    }

    public int getNbDiamonds() {
        return nbDiamonds;
    }

    public IabHelper getmHelper() {
        return mHelper;
    }

    public void incrementNbDiamonds (int plusDiamonds)
    {
        this.nbDiamonds += plusDiamonds;
        gF.updateValeur(mFichier, DIAMONDS, String.valueOf(nbDiamonds));
    }

    public void incrementNbLifes (int plusLifes)
    {
        if (plusLifes < 0)
        {
            showDialog=true;
        }
        this.nbLifes += plusLifes;
        gF.updateValeur(mFichier, LIFES, String.valueOf(nbLifes));
    }

    public void setmNiveau(int mNiveau) {
        this.mNiveau = mNiveau;
        loadBestScore();
    }

    public int getmReachedLevel() {
        return mReachedLevel;
    }

    public void incrementNiveau ()
    {
        this.mNiveau ++;
        gF.updateValeur(mFichier, NIVEAU, String.valueOf(mNiveau));
        mRepeatLevel=0;
        loadBestScore();

        if (mReachedLevel < mNiveau)
        {
            gF.updateValeur(mFichier, REACHED_LEVEL, mNiveau + "");
            mReachedLevel = mNiveau;
        }

        // check for achievements
        checkForAchievements("LEVEL",mNiveau);

        // push those accomplishments to the cloud, if signed in
        pushAccomplishments();


    }

    private void loadBestScore() {
        try {
            bestScore = Long.valueOf(gF.selectValeur(mFichierScore, "*NIV" + mNiveau + ":"));
        } catch (NumberFormatException e)
        {
            bestScore=0;
        }
    }

    void checkForAchievements(String type, int number) {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
        if (type.compareTo("LEVEL")==0)
        {
            if (number == 11) {
                mOutbox.mBeginnerAchievement = true;
                myAchievementToast(getString(R.string.achievement_beginner_toast_text));
            }
            else if (number == 21) {
                mOutbox.mIntermediateAchievement = true;
                myAchievementToast(getString(R.string.achievement_intermediate_toast_text));
            }
            else if (number == 31) {
                mOutbox.mProficientAchievement = true;
                myAchievementToast(getString(R.string.achievement_proficient_toast_text));
            }
            else if (number == 41)
            {
                mOutbox.mExperiencedAchievement = true;
                myAchievementToast(getString(R.string.achievement_experienced_toast_text));
            }
            else if (number == 51)
            {
                mOutbox.mAdvancedAchievement = true;
                myAchievementToast(getString(R.string.achievement_advanced_toast_text));
            }
            else if (number == 80)
            {
                mOutbox.mSeniorAchievement = true;
                myAchievementToast(getString(R.string.achievement_senior_toast_text));
            }
        }
        else if (type.compareTo("DEAD")==0)
        {
            if (number == 50)
            {
                mOutbox.mGhostAchievement = true;
                myAchievementToast(getString(R.string.achievement_ghost_toast_text));
            } else if (number == 150)
            {
                mOutbox.mZombieAchievement = true;
                myAchievementToast(getString(R.string.achievement_zombie_toast_text));
            }
            else if (number == 300)
            {
                mOutbox.mUndeadAchievement = true;
                myAchievementToast(getString(R.string.achievement_undead_toast_text));
            }
            else if (number == 500)
            {
                mOutbox.mImmortalAchievement = true;
                myAchievementToast(getString(R.string.achievement_immortal_toast_text));
            }
        }


        mOutbox.mBoredSteps++;
    }

    void unlockAchievement(int achievementId, final String fallbackString) {
        if (googleApiClientIsConnected()) {
            Games.Achievements.unlock(mGoogleApiClient, getString(achievementId));
        } else {

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, getString(R.string.achievement) + ": " + fallbackString,
                            Toast.LENGTH_LONG).show();
                }
            });

        }
    }



    public void pushScoreToPalmares ()
    {

        if (googleApiClientIsConnected()) {
            // can't push to the cloud, so save locally
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_score_palmares), totalScore);
            return;
        }
    }

    void pushAccomplishments() {
        if (!googleApiClientIsConnected()) {
            // can't push to the cloud, so save locally
            mOutbox.saveLocal(mActivity);
            return;
        }

        if (mOutbox.mBeginnerAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_beginner));
            mOutbox.mBeginnerAchievement = false;
        }
        if (mOutbox.mIntermediateAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_intermediate));
            mOutbox.mIntermediateAchievement = false;
        }
        if (mOutbox.mProficientAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_proficient));
            mOutbox.mProficientAchievement = false;
        }
        if (mOutbox.mExperiencedAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_experienced));
            mOutbox.mExperiencedAchievement = false;
        }
        if (mOutbox.mAdvancedAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_advanced));
            mOutbox.mAdvancedAchievement = false;
        }
        if (mOutbox.mSeniorAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_senior));
            mOutbox.mSeniorAchievement = false;
        }
        if (mOutbox.mGhostAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_ghost));
            mOutbox.mGhostAchievement = false;
        }
        if (mOutbox.mZombieAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_zombie));
            mOutbox.mZombieAchievement = false;
        }
        if (mOutbox.mUndeadAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_undead));
            mOutbox.mUndeadAchievement = false;
        }
        if (mOutbox.mImmortalAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_immortal));
            mOutbox.mImmortalAchievement = false;
        }


        mOutbox.saveLocal(mActivity);
    }

    public boolean ismSoundsEnabled() {
        return mSoundsEnabled;
    }

    public void setmSoundsEnabled(boolean mSoundsEnabled) {
        this.mSoundsEnabled = mSoundsEnabled;
        gF.updateValeur(mFichier,SOUNDS,mSoundsEnabled+"");
    }

    protected boolean googleApiClientIsConnected() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    void complain(String message) {

    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(mActivity);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    protected Person getUserInfo () {
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null)
            return (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient));
            else
            return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mSignInClicked == true)
        {
            Settings.onConnectedUpdateUi();
            mSignInClicked = false;
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(mActivity, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            //showErrorDialog(result.getErrorCode());
            mResolvingError = true;
            Settings.onConnectedFailedUpdateUi();
        }



    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        if (mGoogleApiClient!=null)
            mGoogleApiClient.connect();
    }

    public void disposemHelper() {
        mHelper.dispose();
        mHelper = null;
    }

    protected void connectGoogleGames() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected() && connectIsEnabled && isNetworkAvailable())
        {
            mGoogleApiClient.connect();
        }
    }

    public void disconnectGoogleGames() {
        if (mGoogleApiClient!=null && mGoogleApiClient.isConnected())
        mGoogleApiClient.disconnect();
    }

    public void handleActivityResultForGoogleGames(int requestCode, int resultCode,Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == mActivity.RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }



        /*
        if (requestCode == RC_SIGN_IN) {
            mResolvingConnectionFailure = false;
            if (resultCode == mActivity.RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(mActivity,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }*/
    }

    public void showAchievementsIntent() {
        if (googleApiClientIsConnected()) {
            mActivity.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    REQUEST_ACHIEVEMENTS);
        } else {
            BaseGameUtils.makeSimpleDialog(mActivity, getString(R.string.achievements_not_available)).show();
        }
    }

    public void signOutGoogleGames() {
        mGoogleApiClient.disconnect();
    }

    public void unlockLevelDialog()
    {
        CustomDialogUnlockLevel unlockLevel = new CustomDialogUnlockLevel(mActivity, mRepeatLevel);
        unlockLevel.setDialogResult(new CustomDialogUnlockLevel.MyDialogListener() {
            @Override
            public void unlock() {
                if (nbDiamonds > 9)
                {
                    incrementNbDiamonds(-10);
                    incrementNiveau();

                } else {

                    purchaseDialog();

                }
            }
        });
        unlockLevel.show();
    }


    public void incrementReachedNiveau() {
        mReachedLevel++;
        gF.updateValeur(mFichier, REACHED_LEVEL, mReachedLevel + "");
    }

    public void incrementIsDead()
    {
        isDead++;

        // check for achievements
        checkForAchievements("DEAD", isDead);
    }

    public void saveLifes ()
    {
        gF.updateValeur(mFichier,LIFES,nbLifes+"");
    }

    public void saveIsDead()
    {
        gF.updateValeur(mFichier, IS_DEAD, isDead + "");
    }

    public long getBestScore() {
        return bestScore;
    }

    public void saveBestScore (long bestScore)
    {
        this.bestScore = bestScore;
        this.totalScore+=bestScore;
        gF.updateValeur(mFichierScore,"*NIV"+mNiveau+":",bestScore+"");
        gF.updateValeur(mFichier,TOTAL_SCORE,totalScore+"");
    }

    public void showPalmaresIntent() {
        if (googleApiClientIsConnected()) {
            mActivity.startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    getString(R.string.leaderboard_score_palmares)), REQUEST_LEADERBOARD);

        } else {
            BaseGameUtils.makeSimpleDialog(mActivity, getString(R.string.achievements_not_available)).show();
        }
    }

    public boolean getShowDialog() {
        return showDialog;
    }

    public void setShowDialog(boolean showDialog) {
        this.showDialog = showDialog;
    }

    class AccomplishmentsOutbox {
        boolean mBeginnerAchievement = false;
        String beg = "*B:";
        boolean mIntermediateAchievement = false;
        String inter = "*I:";
        boolean mProficientAchievement = false;
        String pro = "*P:";
        boolean mExperiencedAchievement = false;
        String exp = "*E:";
        boolean mAdvancedAchievement = false;
        String adv = "*A:";
        boolean mSeniorAchievement = false;
        String sen = "*S:";
        boolean mGhostAchievement=false;
        String gho = "*G:";
        boolean mZombieAchievement=false;
        String zom = "*Z:";
        boolean mUndeadAchievement=false;
        String und = "*U:";
        boolean mImmortalAchievement=false;
        String imm = "*Im:";

        int mBoredSteps = 0;
        int mEasyModeScore = -1;
        int mHardModeScore = -1;

        boolean isEmpty() {
            return !mBeginnerAchievement && !mIntermediateAchievement && !mProficientAchievement &&
                    !mExperiencedAchievement && !mAdvancedAchievement && !mSeniorAchievement &&!mZombieAchievement && !mGhostAchievement && mBoredSteps == 0 && mEasyModeScore < 0 &&
                    mHardModeScore < 0;
        }

        public void saveLocal(Context ctx) {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
            gF.updateValeur(mAchievementsFile, beg, mBeginnerAchievement+"");
            gF.updateValeur(mAchievementsFile,inter,mIntermediateAchievement+"");
            gF.updateValeur(mAchievementsFile,exp,mExperiencedAchievement+"");
            gF.updateValeur(mAchievementsFile,pro,mProficientAchievement+"");
            gF.updateValeur(mAchievementsFile,adv,mAdvancedAchievement+"");
            gF.updateValeur(mAchievementsFile,sen,mSeniorAchievement+"");
            gF.updateValeur(mAchievementsFile,gho,mGhostAchievement+"");
            gF.updateValeur(mAchievementsFile,zom,mZombieAchievement+"");
            gF.updateValeur(mAchievementsFile,und,mUndeadAchievement+"");
            gF.updateValeur(mAchievementsFile,imm,mImmortalAchievement+"");
        }

        public void loadLocal(Context ctx) {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
            if (!gF.existe(mAchievementsFile))
            {
                gF.creeFichier(mAchievementsFile);
                gF.updateValeur(mAchievementsFile, beg, "false");
                gF.updateValeur(mAchievementsFile,inter,"false");
                gF.updateValeur(mAchievementsFile,exp,"false");
                gF.updateValeur(mAchievementsFile,pro,"false");
                gF.updateValeur(mAchievementsFile,adv,"false");
                gF.updateValeur(mAchievementsFile,sen,"false");
                gF.updateValeur(mAchievementsFile,gho,"false");
                gF.updateValeur(mAchievementsFile,zom,"false");
                gF.updateValeur(mAchievementsFile,und,"false");
                gF.updateValeur(mAchievementsFile,imm,"false");

            }
            else
            {
                mBeginnerAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,beg));
                mIntermediateAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,inter));
                mExperiencedAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,exp));
                mProficientAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,pro));
                mAdvancedAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,adv));
                mSeniorAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,sen));
                mGhostAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,gho));
                mZombieAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,zom));
                mUndeadAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,und));
                mImmortalAchievement = Boolean.valueOf(gF.selectValeur(mAchievementsFile,imm));




            }
        }
    }
}
