package com.xplocity.xplocity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import api_classes.SignUpUploader;
import api_classes.interfaces.SignUpUploaderInterface;
import app.XplocityApplication;
import models.AuthToken;
import utils.UI.WaitWheel;


public class SignUpActivity
        extends AppCompatActivity
        implements SignUpUploaderInterface {

    TextView mEmailInput;
    TextView mPasswordInput;
    TextView mErrorsTxt;
    Button mSignUpBtn;
    private WaitWheel mWaitWheel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmailInput = findViewById(R.id.txt_email);
        mPasswordInput = findViewById(R.id.txt_password);
        mSignUpBtn = findViewById(R.id.btn_sign_up);
        mErrorsTxt = findViewById(R.id.txtError);
        mWaitWheel = new WaitWheel((FrameLayout) findViewById(R.id.waitWheel), this);

        mSignUpBtn.setEnabled(false);

        TextWatcher watcher = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                changeSignUpBtnStyle();
            }
        };
        mEmailInput.addTextChangedListener(watcher);
        mPasswordInput.addTextChangedListener(watcher);

    }


    public void redirectToSignIn(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void changeSignUpBtnStyle() {
        if ((mEmailInput.getText().length() > 0) && (mPasswordInput.getText().length() > 0)) {
            mSignUpBtn.setTextColor(getResources().getColor(R.color.btnColoredText));
            mSignUpBtn.setBackgroundColor(getResources().getColor(R.color.btnColoredBackground));
            mSignUpBtn.setEnabled(true);
        }
        else {
            mSignUpBtn.setTextColor(getResources().getColor(R.color.btnColoredDisabledText));
            mSignUpBtn.setBackgroundColor(getResources().getColor(R.color.btnColoredDisabledBackground));
            mSignUpBtn.setEnabled(false);
        }
    }


    public void signUp(View v) {
        mWaitWheel.showWaitAnimation();
        String email = mEmailInput.getText().toString();
        String password = mPasswordInput.getText().toString();


        SignUpUploader builder = new SignUpUploader(this);
        builder.uploadSignUpInfo(email, password);
    }


    @Override
    public void onSuccessUploadSignUpInfo(AuthToken authToken) {
        mErrorsTxt.setText("");

        if (authToken != null) {
            // TODO мб вынести работу с хранилищем в отдельный класс?
            SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("user_id", authToken.user_id);
            editor.putString("auth_token", authToken.auth_token);
            editor.commit();

            Toast toast = Toast.makeText(XplocityApplication.getAppContext(), "Registration successful", Toast.LENGTH_LONG);
            toast.show();

            redirectToMainActivity();

        }

        mWaitWheel.hideWaitAnimation();
    };

    @Override
    public void onErrorUploadSignUpInfo(ArrayList<String> errors) {
        String error = "";
        for (String e: errors) {
            error = error + e;
            if (e != errors.get(errors.size()-1)) {
                error = error + System.lineSeparator();
            }
        }

        mErrorsTxt.setText(error);

        mWaitWheel.hideWaitAnimation();

    };


    // TODO редиректы вынести отдельно?
    private void redirectToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), RoutesListActivity.class);
        startActivity(intent);
    }

}
