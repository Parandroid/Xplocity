package com.xplocity.xplocity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.util.ArrayList;

import api_classes.ResetPasswordUploader;
import api_classes.SendResetPasswordMailUploader;
import api_classes.interfaces.ResetPasswordUploaderInterface;
import api_classes.interfaces.SendResetPasswordMailUploaderInterface;
import utils.UI.WaitWheel;

public class ResetPasswordActivity extends AppCompatActivity
        implements SendResetPasswordMailUploaderInterface,
        ResetPasswordUploaderInterface {

    ViewAnimator mViewAnimator;
    TextView mEmailInput;
    TextView mPasswordResetTokenInput;
    TextView mNewPasswordInput;
    Button mSendResetPasswordEmailBtn;
    Button mResetPasswordBtn;

    TextView mErrorsTxt;
    private WaitWheel mWaitWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mViewAnimator = findViewById(R.id.view_animator);
        mSendResetPasswordEmailBtn = findViewById(R.id.btn_send_reset_password_email);
        mEmailInput = findViewById(R.id.txt_email);
        mPasswordResetTokenInput = findViewById(R.id.txt_password_reset_token);
        mNewPasswordInput = findViewById(R.id.txt_new_password);
        mResetPasswordBtn = findViewById(R.id.btn_set_new_password);

        mSendResetPasswordEmailBtn.setEnabled(false);
        TextWatcher watcherEmail = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                changeSendResetPasswordEmailBtnStyle();
            }
        };
        mEmailInput.addTextChangedListener(watcherEmail);

        mResetPasswordBtn.setEnabled(false);
        TextWatcher watcherPassword = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                changeResetPasswordBtnStyle();
            }
        };
        mPasswordResetTokenInput.addTextChangedListener(watcherPassword);
        mNewPasswordInput.addTextChangedListener(watcherPassword);

        mErrorsTxt = findViewById(R.id.txtError);
        mWaitWheel = new WaitWheel((FrameLayout) findViewById(R.id.waitWheel), this);

    }

    public void changeSendResetPasswordEmailBtnStyle() {
        if (mEmailInput.getText().length() > 0) {
            mSendResetPasswordEmailBtn.setEnabled(true);
        } else {
            mSendResetPasswordEmailBtn.setEnabled(false);
        }
    }

    public void changeResetPasswordBtnStyle() {
        if ((mPasswordResetTokenInput.getText().length() > 0) && (mNewPasswordInput.getText().length() > 0)) {
            mResetPasswordBtn.setEnabled(true);
        } else {
            mResetPasswordBtn.setEnabled(false);
        }
    }


    public void sendResetPasswordEmail(View view) {
        String email = mEmailInput.getText().toString();
        SendResetPasswordMailUploader builder = new SendResetPasswordMailUploader(this);
        builder.sendResetPasswordMail(email);
    }


    public void resetPassword(View view) {
        mWaitWheel.showWaitAnimation();

        String token = mPasswordResetTokenInput.getText().toString();
        String password = mNewPasswordInput.getText().toString();
        ResetPasswordUploader builder = new ResetPasswordUploader(this);
        builder.resetPassword(password, token);
    }

    public void redirectToSignIn() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSuccessSentMail() {
        mViewAnimator.showNext();
    }

    @Override
    public void onSuccessResetPassword() {
        Toast.makeText(this, R.string.toast_new_password, Toast.LENGTH_SHORT).show();
        mWaitWheel.hideWaitAnimation();
        redirectToSignIn();

    }

    @Override
    public void onErrorResetPassword(ArrayList<String> errors) {
        String error = "";
        for (String e: errors) {
            error = error + e;
            if (e != errors.get(errors.size()-1)) {
                error = error + System.lineSeparator();
            }
        }

        mErrorsTxt.setText(error);
        mErrorsTxt.setVisibility(View.VISIBLE);

        mWaitWheel.hideWaitAnimation();
    }
}