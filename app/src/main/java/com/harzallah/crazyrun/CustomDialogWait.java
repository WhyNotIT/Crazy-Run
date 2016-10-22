package com.harzallah.crazyrun;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Harzallah on 23/10/2015.
 */
public class CustomDialogWait extends Dialog implements
        android.view.View.OnClickListener {

    private int msg;
    public Activity mActivity;
    public Button ok;
    private String msg1 ="Wait 3 minutes to get new life\nor connect to internet and buy new Diamonds.";
    private String msg2 ="Connect to internet and buy new Diamonds.";
    private TextView mMSg;

    public CustomDialogWait(Activity mActivity,int msg) {
        super(mActivity);
        // TODO Auto-generated constructor stub
        this.mActivity = mActivity;
        this.msg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_wait);
        mMSg = (TextView) findViewById(R.id.wait_dialog_msg);
        if (msg == 1)
        {
            mMSg.setText(msg1);
        }
        else if (msg == 2)
        {
            mMSg.setText(msg2);
        }
        setCancelable(false);
        ok = (Button) findViewById(R.id.btn_ok);
        ok.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                GlobalState.showAD();
                dismiss();
                break;
            default:
                break;
        }
    }

}
