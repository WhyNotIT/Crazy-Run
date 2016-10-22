package com.harzallah.crazyrun;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Harzallah on 28/10/2015.
 */
public class CustomDialogUnlockLevel extends Dialog implements
        android.view.View.OnClickListener {

    public Activity mActivity;
    public Button unlock;
    public Button cancel;
    private String msg;
    private TextView mMSg;
    private MyDialogListener mDialogListener;


    public CustomDialogUnlockLevel(Activity mActivity,int mRepeatLevel) {
        super(mActivity);
        // TODO Auto-generated constructor stub
        this.mActivity = mActivity;
        this.msg = "Are you blocked in this level ?\n( you died "+mRepeatLevel+ " times )\nUnlock it for only 10 Diamonds.";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_unlock_level);
        mMSg = (TextView) findViewById(R.id.unlock_dialog_msg);
        mMSg.setText(msg);
        setCancelable(false);
        unlock = (Button) findViewById(R.id.btn_unlock);
        unlock.setOnClickListener(this);

        cancel = (Button) findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(this);

    }

    public interface MyDialogListener
    {
        void unlock ();
    }

    public void setDialogResult(MyDialogListener dialogResult){
        mDialogListener = dialogResult;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_unlock:
                mDialogListener.unlock();
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

}
