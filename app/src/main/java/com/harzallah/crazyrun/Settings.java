package com.harzallah.crazyrun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.plus.model.people.Person;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harzallah on 25/10/2015.
 */
public class Settings extends Activity {

    private static com.google.android.gms.common.SignInButton signIn;
    private static Button signOut;
    private static Button achievements,palmares;
    private static GlobalState mState;
    private static TextView status;
    private static ImageView mPhotoUser;
    private Activity mActivity;
    private ToggleButton mSoundStatus;
    private Spinner mSpinnerLevel;
    private Button unlockLevelButton;
    int i = 1;
    private List<Integer> list;
    private ArrayAdapter<Integer> dataAdapter;
    private Tracker mTracker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mState = ((GlobalState) getApplicationContext());
        mActivity = this;

        status = (TextView) findViewById(R.id.status);
        mSoundStatus = (ToggleButton) findViewById(R.id.sounds);
        mSoundStatus.setChecked(mState.ismSoundsEnabled());
        mSoundStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if (mSoundStatus.isChecked())
                   mState.setmSoundsEnabled(true);
                else
                   mState.setmSoundsEnabled(false);
            }
        });
        signIn = (com.google.android.gms.common.SignInButton) findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInClicked();
            }
        });
        signOut = (Button) findViewById(R.id.sign_out_button);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mState.googleApiClientIsConnected()) {

                    onSignOutClicked();
                }
            }
        });

        achievements = (Button) findViewById(R.id.achievements);
        achievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mState.showAchievementsIntent();
            }
        });

        palmares = (Button) findViewById(R.id.palmares);
        palmares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mState.showPalmaresIntent ();
            }
        });

        mPhotoUser = (ImageView)findViewById(R.id.photo_user);

        if (mState.googleApiClientIsConnected())
        {
            setUserInfo();
            signIn.setVisibility(View.GONE);
            signOut.setVisibility(View.VISIBLE);
            achievements.setVisibility(View.VISIBLE);
            palmares.setVisibility(View.VISIBLE);
            mPhotoUser.setVisibility(View.VISIBLE);
        }
        else
        {
            status.setText("Login to submit your achievements!");
            signIn.setVisibility(View.VISIBLE);
            signOut.setVisibility(View.GONE);
            achievements.setVisibility(View.GONE);
            palmares.setVisibility(View.GONE);
            mPhotoUser.setVisibility(View.GONE);
        }

        list = new ArrayList<Integer>();

        while (i <= mState.getmReachedLevel())
        {
            list.add(i++);
        }

        dataAdapter = new ArrayAdapter<Integer>
                (this, android.R.layout.simple_spinner_item,list);

        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        mSpinnerLevel = (Spinner) findViewById(R.id.level_choice);
        mSpinnerLevel.setAdapter(dataAdapter);
        mSpinnerLevel.setSelection(mState.getNiveau() - 1);
        mSpinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mState.setmNiveau((int) mSpinnerLevel.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        unlockLevelButton = (Button) findViewById(R.id.unlock_level);
        unlockLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState.getNbDiamonds()> 9)
                {
                    mState.incrementNbDiamonds(-10);
                    mState.incrementReachedNiveau();


                    i=1;
                    list.clear();
                    while (i <= mState.getmReachedLevel())
                    {
                        list.add(i++);
                    }

                    dataAdapter = new ArrayAdapter<Integer>(getApplicationContext(),android.R.layout.simple_spinner_item,list);

                    dataAdapter.setDropDownViewResource
                            (android.R.layout.simple_spinner_dropdown_item);
                    mSpinnerLevel.setAdapter(dataAdapter);
                    mSpinnerLevel.setSelection(mState.getmReachedLevel() - 1);
                    mState.setmNiveau(mState.getmReachedLevel());




                } else {

                    mState.purchaseDialog();

                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mTracker = mState.getDefaultTracker();
    }

    private void onSignOutClicked() {
        // Clear the default account so that GoogleApiClient will not automatically
        // connect in the future.
        mState.disableConnection();
        if (mState.googleApiClientIsConnected()) {
            mState.signOutGoogleGames();
        }

        // show sign-in button, hide the sign-out button
        status.setText("Login to submit your achievements!");
        signIn.setVisibility(View.VISIBLE);
        signOut.setVisibility(View.GONE);
        achievements.setVisibility(View.GONE);
        palmares.setVisibility(View.GONE);
        mPhotoUser.setVisibility(View.GONE);
    }

    public static void onConnectedUpdateUi ()
    {
        setUserInfo();
        signIn.setVisibility(View.GONE);
        mPhotoUser.setVisibility(View.VISIBLE);
        signOut.setVisibility(View.VISIBLE);
        achievements.setVisibility(View.VISIBLE);
        palmares.setVisibility(View.VISIBLE);

    }

    public static void onConnectedFailedUpdateUi ()
    {
        if (status != null)
        status.setText("Connecetion Failed!");
    }

    public static void setUserInfo ()
    {
        Person user = mState.getUserInfo();
        status.setText("Connected as: " + user.getName().getGivenName() + " " + user.getName().getFamilyName() + ".");
        Log.e(mState.getTAG(),user.getImage().getUrl().replace("?sz=50","?sz=100"));
        // show The Image
        new DownloadImageTask(mPhotoUser)
                .execute(user.getImage().getUrl());


    }

    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mState.enableConnection();
        mState.mShouldResolve = true;
        mState.mSignInClicked = true;
        mState.connectGoogleGames();

        // Show a message to the user that we are signing in.
        status.setText("Connecting...");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(mState.getTAG(), "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == mState.RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mState.mShouldResolve = false;
            }

            mState.mIsResolving = false;
            mState.connectGoogleGames();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mState.setmActivity(this);
        mTracker.setScreenName("Setting Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }









private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}



}
