package com.harzallah.crazyrun;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;

/**
 * Created by Harzallah on 22/10/2015.
 */
public class CustomDialogDiamonds extends Dialog implements
        android.view.View.OnClickListener {

    public static final String TAG = "CR";

    public Activity mActivity;
    public Dialog mDialog;
    public Button yes, no;
    private RadioButton switch4,switch8;
    private MyDialogListener mDialogListener;
    private RadioButton priceDiamonds,priceDiamonds20,priceDiamonds50;
    private String priceR1, priceR2, priceR3;




    public CustomDialogDiamonds(Activity mActivity,String priceR1, String priceR2,String priceR3) {
        super(mActivity);
        // TODO Auto-generated constructor stub
        this.mActivity = mActivity;
        this.priceR1 = priceR1;
        this.priceR2 = priceR2;
        this.priceR3 = priceR3;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_diamonds);
        setCancelable(false);


        priceDiamonds = (RadioButton) findViewById(R.id.price);
        priceDiamonds.setText(priceR1);
        priceDiamonds20 = (RadioButton) findViewById(R.id.price20);
        priceDiamonds20.setText(priceR2);
        priceDiamonds50 = (RadioButton) findViewById(R.id.price50);
        priceDiamonds50.setText(priceR3);


        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);



    }

    public interface MyDialogListener
    {
        void selectedChoise(int value);
    }


    public void setDialogResult(MyDialogListener dialogResult){
        mDialogListener = dialogResult;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:

                if (priceDiamonds50.isChecked()) {
                    mDialogListener.selectedChoise(50);
                } else if (priceDiamonds20.isChecked()) {
                    mDialogListener.selectedChoise(20);
                } else {
                    mDialogListener.selectedChoise(10);
                }

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