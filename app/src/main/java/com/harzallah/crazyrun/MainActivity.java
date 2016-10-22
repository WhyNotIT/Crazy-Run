package com.harzallah.crazyrun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class MainActivity extends Activity {



    private TextView mNiveau;
    private ImageView mPlayBouton ;
    private static TextView nbLifesView;
    private TextView nbDiamondsView;
    private ImageView settingsView;
    private int mEcranHauteur,mEcranLargeur;




    private Activity mActivity;



    //GlobalState
    static GlobalState mState;

    private RelativeLayout whyNot;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        new Thread(new Runnable() {

            @Override
            public void run() {
                whyNot = (RelativeLayout) findViewById(R.id.why_not);
                whyNot.setVisibility(View.VISIBLE);
                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException e)
                {
                    whyNot.setVisibility(View.GONE);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        whyNot.setVisibility(View.GONE);
                        mState.connectGoogleGames();

                    }
                });

            }
        }).start();


        mState = ((GlobalState) getApplicationContext());

        mActivity = this;

        Display ecran = getWindowManager().getDefaultDisplay();
        mEcranHauteur = ecran.getHeight();
        mEcranLargeur = ecran.getWidth();

        nbLifesView = (TextView) findViewById(R.id.nb_lifes);
        nbLifesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nbLifesView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));
                mState.getLifes ();
            }
        });

        nbDiamondsView = (TextView) findViewById(R.id.nb_diamonds);
        nbDiamondsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nbDiamondsView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));
                    mState.purchaseDialog();
            }
        });

        settingsView = (ImageView) findViewById(R.id.settings);
        settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));
                Intent mSettingIntetnt = new Intent(MainActivity.this, Settings.class);
                MainActivity.this.startActivity(mSettingIntetnt);
            }
        });


        mNiveau = (TextView) findViewById(R.id.niveau);

        mPlayBouton = (ImageView) findViewById(R.id.play_bouton);
        mPlayBouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mState.getNbLifes() > 0) {
                    mPlayBouton.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));                    Intent mPlayIntetnt = new Intent(MainActivity.this, Play.class);
                    MainActivity.this.startActivity(mPlayIntetnt);
                } else if (mState.getNbDiamonds() > 3) {
                    mState.getLifes();
                } else {
                    if (mState.isNetworkAvailable()) {
                        mState.purchaseDialog();
                    } else {
                        mState.waitDialog();
                    }
                }

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("PLAY!")
                        .build());

            }
        });



    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        // very important:
        if (mState.getmHelper() != null) {
            mState.disposemHelper();
        }

        mState.disconnectGoogleGames();

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        mState.handleActivityResultForGoogleGames (requestCode,resultCode,data);

        if (mState.getmHelper() == null) return;

        // Pass on the activity result to the helper for handling
        if (!mState.getmHelper().handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
        }
    }







    @Override
    protected void onStart() {
        mTracker = mState.getDefaultTracker();
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {

            nbLifesView.setText(""+mState.getNbLifes());
            nbDiamondsView.setText(""+mState.getNbDiamonds());
            mNiveau.setText("" + mState.getNiveau());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mState.setmActivity(this);
        mState.setLifes();
        nbLifesView.setText("" + mState.getNbLifes());
        nbDiamondsView.setText("" + mState.getNbDiamonds());
        mPlayBouton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.play_button));

        if (mState.getShowDialog()) {
            if (mState.getNbLifes() < 1 && mState.getNbDiamonds() > 3) {
                mState.getLifes();
            } else if (mState.getNbLifes() < 1 && mState.getNbDiamonds() < 4) {
                if (mState.isNetworkAvailable()) {
                    mState.purchaseDialog();
                }
            }

            mState.setShowDialog(false);

        }

        mTracker.setScreenName("Main Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    protected void onPause() {
        mState.saveLifes();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}

