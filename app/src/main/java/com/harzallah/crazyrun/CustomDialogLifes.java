package com.harzallah.crazyrun;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;

/**
 * Created by Harzallah on 21/10/2015.
 */
public class CustomDialogLifes extends Dialog implements
        android.view.View.OnClickListener {

    public Activity mActivity;
    public Dialog mDialog;
    public Button yes, no;
    private RadioButton switch4,switch8;
    private MyDialogListener mDialogListener;


    public CustomDialogLifes(Activity mActivity) {
        super(mActivity);
        // TODO Auto-generated constructor stub
        this.mActivity = mActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_lifes);
        setCancelable(false);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        switch4 = (RadioButton) findViewById(R.id.switch_4d);
        switch8 = (RadioButton) findViewById(R.id.switch_8d);

    }

    public interface MyDialogListener
    {
        void selectedChoise (int value);
    }

    public void setDialogResult(MyDialogListener dialogResult){
        mDialogListener = dialogResult;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                if (switch4.isChecked())
                mDialogListener.selectedChoise(4);
                else
                mDialogListener.selectedChoise(8);
                break;
            case R.id.btn_no:
                GlobalState.showAD();
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}