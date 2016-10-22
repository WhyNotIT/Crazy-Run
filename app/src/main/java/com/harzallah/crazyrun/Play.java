package com.harzallah.crazyrun;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class Play extends Activity {




    //GlobalState
    GlobalState mState;

    private boolean isRunning = true;


    // Main view
    private RelativeLayout mFrameCars;
    private RelativeLayout mFrame;

    // Bubble image

    // Display dimensions



    // Gesture Detector
    private GestureDetector mGestureDetector;
    private PlayerView mPlayer;
    private int mMode = 0;
    private Collision mCollisionManager;

    private int mEcranHauteur,mEcranLargeur;

    private ImageView carrefour1,carrefour2,carrefour3,finish;
    private TextView nbLifesView,nbDiamondsView;
    private boolean airBoolean = true;
    private TextView levelTextView;
    private int maxVolume = 10;


    private Chronometer mChronometer;
    private boolean mChronometerIsRunning=false;
    private Thread scoreThread;
    private boolean noView;
    private int mDisplayWidth,mDisplayHeight;
    private Tracker mTracker;

    public enum Direction {
        Droite,
        Gauche
    }


    //PARAMETRES:
    private float distanceBetweenCars;
    private float mPasCars;
    private int mouvementUntilChangeSpeed;
    private int deviseur;
    private int mRandom;
    private ImageView settingsView;

    List<AutoView> l1,l2,l3;

    private TextView scoreView,bestScore;
    private ImageView bestScoreAnim;


    // Sound variables

    // AudioManager
    private AudioManager mAudioManager;
    // SoundPool
    private SoundPool mSoundPool;
    // ID for the bubble popping sound
    private int mSoundID;
    // Audio volume
    private float mStreamVolume;

    private AdView adView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mState = ((GlobalState) getApplicationContext());

        noView = true;

        l1 = new ArrayList<AutoView>();
        l2 = new ArrayList<AutoView>();
        l3 = new ArrayList<AutoView>();

        mChronometer = (Chronometer) findViewById(R.id.chronometer);


        carrefour1 = (ImageView) findViewById(R.id.carrefour1);
        carrefour2 = (ImageView) findViewById(R.id.carrefour2);
        carrefour3 = (ImageView) findViewById(R.id.carrefour3);
        finish = (ImageView) findViewById(R.id.finish);

        scoreView = (TextView) findViewById(R.id.score);
        bestScore = (TextView) findViewById(R.id.best_score);
        bestScoreAnim = (ImageView) findViewById(R.id.new_best_score);

        levelTextView = (TextView) findViewById(R.id.level);
        levelTextView.setText(mState.getNiveau() + "");

        nbLifesView = (TextView) findViewById(R.id.nb_lifes);
        nbLifesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nbLifesView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));
                mState.getLifes();
            }
        });

        settingsView = (ImageView) findViewById(R.id.settings);
        settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));
                Intent mSettingIntetnt = new Intent(Play.this, Settings.class);
                Play.this.startActivity(mSettingIntetnt);

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

        Display ecran = getWindowManager().getDefaultDisplay();
        mEcranHauteur = ecran.getHeight();
        mEcranLargeur = ecran.getWidth();


        setupGestureDetector();
        // Set up user interface
        mFrameCars = (RelativeLayout) findViewById(R.id.frame_cars);
        mFrame = (RelativeLayout) findViewById(R.id.frame);
        mPlayer = new PlayerView(getApplicationContext());
        mFrame.addView(mPlayer);
        mPlayer.start();
        mFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mChronometerIsRunning) {
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.start();
                        mChronometerIsRunning = true;
                    }


                    mPlayer.clearAnimation();
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.panda);
                    mPlayer.mScaledBitmap = Bitmap.createScaledBitmap(bitmap, mPlayer.getmBitmapHauteur(), mPlayer.getmBitmapLargeur(), true);
                    mPlayer.cours = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mPlayer.cours = false;

                }

                return true;
            }
        });





        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-6886979529982097/6097846065");
        adView.setAdSize(AdSize.BANNER);

        RelativeLayout layout = (RelativeLayout)findViewById(R.id.ad_banner);
        layout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        adView.loadAd(adRequest);



    }



    private void horn ()
    {

        int x = new Random().nextInt(3);

        // load the sound from res/raw/bubble_pop.wav
        if(x==1) {
            mSoundID = mSoundPool.load(this, R.raw.horn1, 1);
        }
        else if (x==2)
        {
            mSoundID = mSoundPool.load(this, R.raw.horn2, 1);

        }
        else
        {
            mSoundID = mSoundPool.load(this, R.raw.horn3, 1);
        }


    }

    private void win ()
    {
        mSoundID = mSoundPool.load(this, R.raw.win, 1);
    }

    private void crush () {

        int x = new Random().nextInt(2);

        if(x==1) {
            mSoundID = mSoundPool.load(this, R.raw.crush1, 1);
        }
        else
        {
            mSoundID = mSoundPool.load(this, R.raw.crush2, 1);
        }

    }




    public void nextLevel ()
    {
        String [] mParams = mState.getmNiveauParams();
        distanceBetweenCars = Float.valueOf(mParams[0]);
        mPasCars = Float.valueOf(mParams[1]);
        mMode = Integer.valueOf(mParams[2]);
        mouvementUntilChangeSpeed = Integer.valueOf(mParams[3]);
        deviseur = Integer.valueOf(mParams[4]);
        mRandom = Integer.valueOf(mParams[5]);
    }

    private void lunchViewCarrefour1 ()
    {
            final AutoView auto_new = new AutoView(getApplicationContext(), Direction.Gauche, 1);
            l1.add(auto_new);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFrameCars.addView(auto_new);
                }
            });
            auto_new.start();

    }

    private void lunchViewCarrefour2 ()
    {

            final AutoView auto_new = new AutoView(getApplicationContext(), Direction.Droite, 2);
            l2.add(auto_new);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFrameCars.addView(auto_new);
                }
            });
        auto_new.start();

    }

    private void lunchViewCarrefour3 ()
    {

            final AutoView auto_new = new AutoView(getApplicationContext(), Direction.Gauche, 3);
            l3.add(auto_new);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFrameCars.addView(auto_new);
                }
            });
        auto_new.start();

    }

    private void lunchViewCarrefour1X ()
    {
        final AutoView auto_new = new AutoView(getApplicationContext(), Direction.Droite, 1);
        l1.add(auto_new);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrameCars.addView(auto_new);
            }
        });
        auto_new.start();

    }

    private void lunchViewCarrefour2X ()
    {

        final AutoView auto_new = new AutoView(getApplicationContext(), Direction.Gauche, 2);
        l2.add(auto_new);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrameCars.addView(auto_new);
            }
        });
        auto_new.start();

    }

    private void lunchViewCarrefour3X ()
    {

        final AutoView auto_new = new AutoView(getApplicationContext(), Direction.Droite, 3);
        l3.add(auto_new);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrameCars.addView(auto_new);
            }
        });
        auto_new.start();

    }




    @Override
    public void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();

        mState.setmActivity(this);
        mState.setLifes();
        adView.resume();
        nbLifesView.setText("" + mState.getNbLifes());
        nbDiamondsView.setText("" + mState.getNbDiamonds());
                levelTextView.setText(mState.getNiveau() + "");
                bestScore.setText("BEST SCORE\n" + mState.getBestScore());
                mFrame.clearDisappearingChildren();
                mFrameCars.clearDisappearingChildren();

                isRunning = true;
                airBoolean = true;
        carrefour1.setY((float) (mEcranHauteur * 0.2));
                carrefour2.setY((float) (mEcranHauteur * 0.4));
                carrefour3.setY((float) (mEcranHauteur * 0.6));
        finish.setY((float) (mEcranHauteur * 0.22));
                finish.setX((float) (mEcranLargeur * 0.13));
                levelTextView.setY((float) (mEcranHauteur * 0.218));

        bestScore.setY((float) (mEcranHauteur * 0.775));
        bestScoreAnim.setY((float) (mEcranHauteur * 0.30));
                mChronometer.setY((float) (mEcranHauteur * 0.775));

                mCollisionManager = new Collision();



        if (noView) {
            lunchViewAirToLeft();
            lunchViewAirToRight();
            noView = false;
        }

        nextLevel();
        resetCarViews();


        resetChronometer();
        mPlayer.setPlayerPosition();
        mPlayer.postInvalidate();








        // Manage bubble popping sound
        // Use AudioManager.STREAM_MUSIC as stream type

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mStreamVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // make a new SoundPool, allowing up to 10 streams
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        // set a SoundPool OnLoadCompletedListener that calls setupGestureDetector()
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mSoundPool.play(mSoundID, mStreamVolume, mStreamVolume, 1, 0, 1f);
            }
        });


        mTracker.setScreenName("Play Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    public void resetChronometer ()
    {
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometerIsRunning=false;
    }

    private void lunchViewAirToRight()
    {
        final AirView air_new = new AirView(getApplicationContext(), Direction.Droite);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrame.addView(air_new);
            }
        });
        air_new.start();
    }

    private void lunchViewAirToLeft ()
    {
        final AirView air_new = new AirView(getApplicationContext(), Direction.Gauche);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrame.addView(air_new);
            }
        });
        air_new.start();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
            Log.e(mState.getTAG(), "HasFocus!!!");

            nbLifesView.setText(""+mState.getNbLifes());
            nbDiamondsView.setText(""+mState.getNbDiamonds());
            nextLevel();
            //mNiveau.setText("" + mState.getNiveau());

            // Get the size of the display so this view knows where borders are
            mDisplayWidth = mFrame.getWidth();
            mDisplayHeight = mFrame.getHeight();


        }
    }



    // Set up GestureDetector
    private void setupGestureDetector() {

        mGestureDetector = new GestureDetector(this,

                new GestureDetector.SimpleOnGestureListener() {

                    // If a fling gesture starts on a AutoView then change the
                    // AutoView's velocity


                });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // TODO - delegate the touch to the gestureDetector

        mGestureDetector.onTouchEvent(event);

        return false;

    }

    @Override
    protected void onStart() {

        super.onStart();
        mTracker = mState.getDefaultTracker();
    }

    @Override
    protected void onPause() {
        mState.pushScoreToPalmares();
        mState.saveLifes();
        mState.savemRepeatLevel();
        isRunning = false;
        airBoolean = false;
        // Release all SoundPool resources
        mSoundPool.unload(mSoundID);
        mSoundPool.release();
        adView.pause();
        super.onPause();

    }

    private void clearCarViews() {
        int i = 0;
        while (!l1.isEmpty())
        {
            l1.get(i).isMoving = false;
            l1.remove(i);
        }
        while (!l2.isEmpty())
        {
            l2.get(i).isMoving = false;
            l2.remove(i);
        }
        while (!l3.isEmpty())
        {
            l3.get(i).isMoving = false;
            l3.remove(i);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrameCars.removeAllViews();
            }
        });

    }

    @Override
    protected void onStop() {

        mState.saveIsDead();
        super.onStop();
    }

    // AutoView is a View that displays a bubble.
    // This class handles animating, drawing, popping amongst other actions.
    // A new AutoView is created for each bubble on the display

    private class Collision {

        public Collision() {
            super();
        }

        public void start(AutoView aV) {

            if (mPlayer.intersects(aV)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mState.ismSoundsEnabled())
                        crush();
                        mPlayer.cours = false;
                        mPlayer.playerLoose();
                        mState.incrementNbLifes(-1);
                        mState.incrementmRepeatLevel();
                        mState.incrementIsDead();
                        nbLifesView.setText(mState.getNbLifes()+"");
                        if(mState.getNbLifes()==0)
                        {

                            gameOver();
                        }

                        if ((mState.getmRepeatLevel() % 19) == 0)
                        {
                            mState.myInfoToast(getString(R.string.blocked_toast));
                        }

                    }
                });

                mPlayer.setPlayerPosition();
                mPlayer.postInvalidate();
            }

            if (mPlayer.aGagner()) {
                //finish();
            }

        }
    }

    public void gameOver ()
    {
        finish();
    }

    public void myNiveauToast (String msg)
    {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    private class AutoView extends View {

        private int REFRESH_RATE = 20;
        private final Paint mPainter = new Paint();
        private ScheduledFuture<?> mMoverFuture;
        private Bitmap mScaledBitmap;
        private AutoView mThis;
        private boolean mChangementMode;
        private int mChangementPas;


        // location, speed and direction of the bubble
        private float mXPos,mYPos;
        private Bitmap mBitmap=null;
        private int mBitmapLargeur,mBitmapHauteur;
        private Direction mDirection;
        int changer;
        private boolean mLunchNextView;
        private int mCarrefour;
        private int mPas;
        private boolean isMoving;
        private Random r;

        public AutoView(Context context,Direction mDirection,int mCarrefour) {
            super(context);
            mThis=this;
            // Create a new random number generator to
            // randomize size, rotation, speed and direction
            this.mCarrefour = mCarrefour;
            this.mDirection = mDirection;
            // Creates the bubble bitmap for this AutoView
            r = new Random();

            initializeView ();


            setCarrefour(mDirection, mCarrefour, r.nextInt(4));


            mPainter.setAntiAlias(true);
        }

        private void initializeView() {

            if (mMode == 8)
            {
                mPasCars = ((float) (r.nextInt(4)+mRandom)) / 1000;
            }

            // Adjust position to center the bubble under user's finger
            if (mDirection == Direction.Gauche)
            {
                mPas= -(int) (mEcranLargeur* mPasCars);
                mXPos = mEcranLargeur;
            }

            else if (mDirection == Direction.Droite )
            {
                mPas= (int) (mEcranLargeur* mPasCars);
                mXPos = 0;
            }

            if (mCarrefour > 3)
                mCarrefour -= 3;

            mChangementPas = mouvementUntilChangeSpeed;
            changer=0;
            mChangementMode=true;
            mLunchNextView = true;
            isMoving=true;
        }

        private void setCarrefour (Direction d,int mCarrefour, int numImage)
        {
            char c = d == Direction.Droite ? 'd' : 'g';
            switch (mCarrefour)
            {

                case 1 : {mYPos=(float) (mEcranHauteur*0.30);mBitmap = setVoiture("voiture"+numImage+"_"+c);break;}
                case 2 : {mYPos=(float) (mEcranHauteur*0.50);mBitmap = setVoiture("voiture"+numImage+"_"+c);break;}
                case 3 : {mYPos=(float) (mEcranHauteur*0.70);mBitmap = setVoiture("voiture"+numImage+"_"+c);break;}
            }
            mBitmapHauteur = mBitmap.getHeight();
            mBitmapLargeur = mBitmap.getWidth();
            mScaledBitmap = Bitmap.createScaledBitmap(mBitmap, mBitmapLargeur, mBitmapHauteur, true);
        }

        public int getmBitmapLargeur() {
            return mBitmapLargeur;
        }

        public int getmBitmapHauteur() {
            return mBitmapHauteur;
        }

        // Start moving the AutoView & updating the display
        private void start() {

            // Creates a WorkerThread
            ScheduledExecutorService executor = Executors
                    .newScheduledThreadPool(1);

            // Execute the run() in Worker Thread every REFRESH_RATE
            // milliseconds
            // Save reference to this job in mMoverFuture
            mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    // TODO - implement movement logic.
                    // Each time this method is run the AutoView should
                    // move one step. If the AutoView exits the display,
                    // stop the AutoView's Worker Thread.
                    // Otherwise, request that the AutoView be redrawn.


                    if( !isOutOfView() && isMoving && isRunning)
                    {
                        mXPos += mPas;
                        //mYPos += (mEcranHauteur / 200);
                        postInvalidate();
                        mCollisionManager.start(mThis);
                        lunchNextView();
                    }




                   if ( mMode < 5)
                    {
                        if (mMode == 4)
                        {
                            Random r = new Random();
                            distanceBetweenCars = r.nextInt(mRandom)+3;
                        }

                        if (changer == mouvementUntilChangeSpeed && mChangementMode)
                        {
                            mPas *= deviseur;
                            mChangementMode = false;
                            changer = 0;
                        }
                        else if (changer == mouvementUntilChangeSpeed && !mChangementMode)
                        {
                            mPas /= deviseur;
                            mChangementMode = true;
                            changer = 0;
                        }
                        else
                        {
                            changer++;
                        }
                    }
                    else if (mMode == 6)
                   {

                       if (changer == mChangementPas && mChangementMode)
                       {
                           mPas *= -1;
                           mChangementMode = false;
                           changer = 0;
                           mChangementPas -=62;
                       }
                       else if (changer == mChangementPas && !mChangementMode)
                       {
                           mPas *= -1;
                           mChangementMode = true;
                           changer = 0;
                           mChangementPas +=62;
                       }
                       else
                       {
                           changer++;
                       }
                   } else if (mMode == 5)
                   {
                       if (changer == mChangementPas && mChangementMode)
                       {
                           mPas *= -1;
                           mChangementMode = false;
                           changer = 0;
                           mChangementPas -=40;
                       }
                       else if (changer == mChangementPas && !mChangementMode)
                       {
                           mPas *= -1;
                           mChangementMode = true;
                           changer = 0;
                           mChangementPas +=40;
                       }
                       else
                       {
                           changer++;
                       }
                   } else if (mMode == 7)
                   {
                       if (changer == mChangementPas && mChangementMode)
                       {
                           mPas *= -1;
                           mChangementMode = false;
                           changer = 0;
                           mChangementPas -=mRandom;
                       }
                       else if (changer == mChangementPas && !mChangementMode)
                       {
                           mPas *= -1;
                           mChangementMode = true;
                           changer = 0;
                           mChangementPas +=mRandom;
                       }
                       else
                       {
                           changer++;
                       }
                   }else if ( mMode == 10)
                    {
                        if (changer == mChangementPas && mChangementMode)
                        {
                            mPas *= deviseur;
                            mChangementMode = false;
                            changer = 0;
                        }
                        else if (changer == mChangementPas && !mChangementMode)
                        {
                            mPas /= deviseur;
                            mChangementMode = true;
                            changer = 0;
                        }
                        else
                        {
                            changer++;
                        }
                    }





                }


            }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
        }


        private void lunchNextView ()
        {
            if (mLunchNextView)
            {


                switch (mCarrefour) {
                    case 1: {
                        if (mDirection == Direction.Gauche && mXPos <= (mEcranLargeur - mBitmapLargeur * distanceBetweenCars)) {
                            useOldView(1);
                            mLunchNextView = false;

                        } else if (mDirection == Direction.Droite && mXPos >= (mBitmapLargeur * distanceBetweenCars))
                        {
                            useOldView(1);
                            mLunchNextView = false;
                        }
                        break;
                    }
                    case 2: {
                        if (mDirection == Direction.Droite && mXPos >= (mBitmapLargeur * distanceBetweenCars))
                        {
                            useOldView(2);
                            mLunchNextView = false;
                        } else if (mDirection == Direction.Gauche && mXPos <= (mEcranLargeur - mBitmapLargeur * distanceBetweenCars)) {
                            useOldView(2);
                            mLunchNextView = false;

                        }
                        break;
                    }
                    case 3: {
                        if (mDirection == Direction.Gauche && mXPos <= (mEcranLargeur - mBitmapLargeur * distanceBetweenCars))
                        {
                            useOldView(3);
                            mLunchNextView = false;
                        } else if (mDirection == Direction.Droite && mXPos >= (mBitmapLargeur * distanceBetweenCars))
                        {
                            useOldView(3);
                            mLunchNextView = false;
                        }
                        break;
                    }
                }
            }




        }

        private void useOldView(int numeroList)
        {
            int i=0;
            if (numeroList == 1)
            {
                while (i<l1.size() && (l1.get(i).isMoving || l1.get(i).mDirection != mDirection)) i++;
                if (i==l1.size())
                {

                    if (mDirection == Direction.Gauche)
                        lunchViewCarrefour1();
                    else
                        lunchViewCarrefour1X();
                }
                else
                {
                    l1.get(i).initializeView();
                }
            } else if (numeroList == 2)
        {
            while (i<l2.size() && (l2.get(i).isMoving || l2.get(i).mDirection != mDirection)) i++;
            if (i==l2.size())
            {
                if (mDirection == Direction.Droite)
                    lunchViewCarrefour2();
                else
                    lunchViewCarrefour2X();
            }
            else
            {
                l2.get(i).initializeView();
            }
        }
            else if (numeroList == 3)
            {
                while (i<l3.size() && (l3.get(i).isMoving || l3.get(i).mDirection != mDirection)) i++;
                if (i==l3.size())
                {
                    if (mDirection == Direction.Gauche)
                        lunchViewCarrefour3();
                    else
                        lunchViewCarrefour3X();                }
                else
                {
                    l3.get(i).initializeView();
                }
            }
        }

        // Cancel the Bubble's movement
        // Remove Bubble from mFrame
        // Play pop sound if the AutoView was popped


        // Draw the Bubble at its current location
        @Override
        protected synchronized void onDraw(Canvas canvas) {

            // TODO - save the canvas
            canvas.save();


            // TODO - draw the bitmap at it's new location
            canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPainter);



            // TODO - restore the canvas
            canvas.restore();


        }

        private boolean isOutOfView() {


            // TODO - Return true if the AutoView has exited the screen
            if (mXPos + mBitmapLargeur < 0 || mXPos > mEcranLargeur )
            {
                isMoving = false;
                return true;
            }


            return false;

        }
    }

    private Bitmap setVoiture(String nomVoiture)
    {
        int idTraget=getResources().getIdentifier("com.harzallah.crazyrun:drawable/" + nomVoiture, null, null);
        return  BitmapFactory.decodeResource(getResources(),idTraget);
    }

    // Do not modify below here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

                return super.onOptionsItemSelected(item);

    }


    private class PlayerView extends View {

        private static final int REFRESH_RATE = 40;
        private final Paint mPainter = new Paint();
        private ScheduledFuture<?> mMoverFuture;
        private Bitmap mScaledBitmap;
        private boolean arrivee=false;
        // location, speed and direction of the bubble
        private float mXPos,mYPos;
        private float mDx;
        private float mDy;
        private int mEcranHauteur,mEcranLargeur,mPas;
        private Boolean cours=false;
        private Bitmap mBitmap=null;
        private int mBitmapLargeur,mBitmapHauteur;

        public PlayerView (Context context) {
            super(context);

            // Create a new random number generator to
            // randomize size, rotation, speed and direction
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.panda);
            mBitmapHauteur = mBitmap.getHeight();
            mBitmapLargeur = mBitmap.getWidth();
            Display ecran = getWindowManager().getDefaultDisplay();
            mEcranHauteur = ecran.getHeight();
            mEcranLargeur = ecran.getWidth();
            // Creates the bubble bitmap for this AutoView
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.panda);
            mScaledBitmap = Bitmap.createScaledBitmap(bitmap, mBitmapHauteur, mBitmapLargeur, true);

            mPas = - (int) (mEcranHauteur*0.012);
            setPlayerPosition();


            mPainter.setAntiAlias(true);
        }


        public int getmBitmapHauteur() {
            return mBitmapHauteur;
        }

        public int getmBitmapLargeur() {
            return mBitmapLargeur;
        }

        private void setPlayerPosition ()
        {
            mXPos = ((float) (mEcranLargeur*0.5));
            mYPos = ((float) (mEcranHauteur*0.80));
        }



        // Start moving the AutoView & updating the display
        private void start() {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    while (!arrivee) {


                        if (cours && !aGagner()) {
                            mYPos += mPas;
                            if (isCloseToCar() && mState.ismSoundsEnabled()) {
                                horn();
                            }
                            postInvalidate();
                        }
                        else if (aGagner())
                        {
                            mChronometer.stop();
                            if (mState.ismSoundsEnabled())
                            win();

                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.panda);
                            mPlayer.mScaledBitmap = Bitmap.createScaledBitmap(bitmap, mPlayer.getmBitmapHauteur(), mPlayer.getmBitmapLargeur(), true);
                            showScore();
                            mState.incrementNiveau();
                                mPlayer.setPlayerPosition();
                            postInvalidate();
                                cours = false;
                                nextLevel ();
                            resetCarViews();


                       }




                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }

        private void showScore() {

                    String tab[] = ((String) mChronometer.getText()).split(":");
                    float secondes = Float.valueOf(tab[1]) + Float.valueOf(tab[0]) * 60;
                    float score = ((1 / secondes) * 1000000) + ((1 / (mState.getmRepeatLevel() + 1)) * 1000000);
                    if ( score > mState.getBestScore())
                    {
                        startNewBestScoreAnimation();
                        mState.saveBestScore((long) score);
                    }
                    showScoreView((long) score);
        }

        private void startNewBestScoreAnimation()
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bestScoreAnim.setVisibility(VISIBLE);
                    bestScoreAnim.bringToFront();
                    bestScoreAnim.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.clignote));
                }
            });

        }

        private void showScoreView(final long score){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scoreView.setVisibility(VISIBLE);
                }
            });

            scoreThread = new Thread() {
                int i = (int) (score / 13);
                int mScore = 0;
                @Override
                public void run() {
                    while (mScore < score) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                scoreView.setText("SCORE\n" + (mScore));
                            }
                        });
                        mScore += i;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scoreView.setText("SCORE \n" + (score));
                        }
                    });


                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bestScoreAnim.setVisibility(GONE);
                            scoreView.setVisibility(GONE);
                            myNiveauToast(mState.getNiveau() + "");
                            levelTextView.setText(mState.getNiveau() + "");
                            bestScore.setText("BEST SCORE\n" + mState.getBestScore()+"");
                            resetChronometer();

                        }
                    });
                }

            };

            scoreThread.run();

        }

        private boolean isCloseToCar() {
            if (mYPos > mEcranHauteur * 0.69 && mYPos < mEcranHauteur * 0.71)
                return true;

            if (mYPos > mEcranHauteur * 0.49 && mYPos < mEcranHauteur * 0.51)
                return true;

            if (mYPos > mEcranHauteur * 0.29 && mYPos < mEcranHauteur * 0.31)
                return true;

            return false;
        }


        public void playerLoose ()
        {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.panda_accident);
            mPlayer.mScaledBitmap = Bitmap.createScaledBitmap(bitmap, mPlayer.getmBitmapHauteur(), mPlayer.getmBitmapLargeur(), true);
            mPlayer.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.clignote));

        }

        // Returns true if the BubbleView intersects position (x,y)
        private synchronized boolean intersects(AutoView aV) {


            Rect mP = new Rect((int) mXPos, (int) mYPos, (int) mXPos + mPlayer.getmBitmapLargeur(), (int) mYPos + mPlayer.getmBitmapHauteur());
            Rect mA = new Rect((int) aV.mXPos, (int) aV.mYPos, (int) aV.mXPos + aV.getmBitmapLargeur() , (int) aV.mYPos + aV.getmBitmapHauteur());

            if (Rect.intersects(mP, mA)) {
                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.panda_pleure);
                //mPlayer.mScaledBitmap = Bitmap.createScaledBitmap(bitmap, mPlayer.getmBitmapHauteur(), mPlayer.getmBitmapLargeur(), true);

                return true;
            }

            return false;
        }

        // Change the Bubble's speed and direction
        private synchronized void deflect(float velocityX, float velocityY) {

            //TODO - set mDx and mDy to be the new velocities divided by the REFRESH_RATE

            mDx = velocityX/REFRESH_RATE;
            mDy = velocityY/REFRESH_RATE;

        }

        // Draw the Bubble at its current location
        @Override
        protected synchronized void onDraw(Canvas canvas) {

            // TODO - save the canvas
            canvas.save();


            // TODO - draw the bitmap at it's new location
            canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPainter);



            // TODO - restore the canvas
            canvas.restore();


        }


        private boolean aGagner() {

            if (mYPos <= mEcranHauteur * 0.17)
            {
                return true;
            }
            return false;
        }
    }

    private void resetCarViews() {
        int i = 0;
        while (!l1.isEmpty())
        {
            l1.get(i).isMoving = false;
            l1.remove(i);
        }
        while (!l2.isEmpty())
        {
            l2.get(i).isMoving = false;
            l2.remove(i);
        }
        while (!l3.isEmpty())
        {
            l3.get(i).isMoving = false;
            l3.remove(i);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrameCars.removeAllViews();
            }
        });

        lunchViewCarrefour1();
        lunchViewCarrefour2();
        lunchViewCarrefour3();

        if (mMode == 9 || mMode == 10) {
            lunchViewCarrefour1X();
            lunchViewCarrefour2X();
            lunchViewCarrefour3X();

        }
    }


    private class AirView extends View {

        private int REFRESH_RATE = 20;
        private final Paint mPainter = new Paint();
        private ScheduledFuture<?> mMoverFuture;
        private Bitmap mScaledBitmap;





        // location, speed and direction of the bubble
        private float mXPos,mYPos;
        private int mPas;
        private Bitmap mBitmap=null;
        private int mBitmapLargeur,mBitmapHauteur;
        private Direction mDirection;

        public AirView(Context context,Direction mDirection) {
            super(context);
            // Create a new random number generator to
            // randomize size, rotation, speed and direction

            this.mDirection = mDirection;
            // Creates the bubble bitmap for this AutoView


            initializeView ();

            mPainter.setAntiAlias(true);
        }

        private void initializeView() {
            Random r = new Random();

            // Adjust position to center the bubble under user's finger
            if (mDirection == Direction.Gauche)
            {
                mPas= -(int) (mEcranLargeur*0.005);
                mXPos = mEcranLargeur;
            }

            else if (mDirection == Direction.Droite )
            {
                mPas= (int) (mEcranLargeur*0.005);
                mXPos = - mBitmapLargeur;
            }

            mYPos = ((float) (mEcranHauteur*0.1));


            setBitmap(mDirection, r.nextInt(2)+1);
        }

        private void setBitmap (Direction d, int numImage)
        {
            char c = d == Direction.Droite ? 'd' : 'g';
            switch (numImage)
            {

                case 1 : {mBitmap = setVoiture("helicoptere_"+c);break;}
                case 2 : {mBitmap = setVoiture("mongolfiere");break;}
            }
            mBitmapHauteur = mBitmap.getHeight();
            mBitmapLargeur = mBitmap.getWidth();
            mScaledBitmap = Bitmap.createScaledBitmap(mBitmap, mBitmapLargeur, mBitmapHauteur, true);
        }




        // Start moving the AutoView & updating the display
        private void start() {

            // Creates a WorkerThread
            ScheduledExecutorService executor = Executors
                    .newScheduledThreadPool(1);

            // Execute the run() in Worker Thread every REFRESH_RATE
            // milliseconds
            // Save reference to this job in mMoverFuture
            mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    // TODO - implement movement logic.
                    // Each time this method is run the AutoView should
                    // move one step. If the AutoView exits the display,
                    // stop the AutoView's Worker Thread.
                    // Otherwise, request that the AutoView be redrawn.




                    if( moveWhileOnScreen()) {
                        stop(false);

                    }

                    postInvalidate();

                    }





            }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);


        }

        private void stop(final boolean popped) {

            initializeView();


        }

        // Draw the Bubble at its current location
        @Override
        protected synchronized void onDraw(Canvas canvas) {

            // TODO - save the canvas
            canvas.save();


            // TODO - draw the bitmap at it's new location
            canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPainter);



            // TODO - restore the canvas
            canvas.restore();


        }


        private synchronized boolean moveWhileOnScreen() {

            // TODO - Move the AutoView
            // Returns true if the AutoView has exited the screen
            mXPos += mPas;

            if (isOutOfView())
                return true;

            return false;

        }

        private boolean isOutOfView() {

            // TODO - Return true if the AutoView has exited the screen
            if (mXPos + mBitmapLargeur < 0)
                return true;

            if (mXPos > mEcranLargeur)
                return true;

            return false;

        }
    }


}
