package com.xplocity.xplocity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import api_classes.AuthTokenDownloader;
import api_classes.interfaces.AuthTokenDownloaderInterface;
import models.AuthToken;
import utils.UI.WaitWheel;

import static services.XplocityPositionService.TRACKING_STATE_ACTIVE;
import static services.XplocityPositionService.TRACKING_STATE_FINISHED;
import static services.XplocityPositionService.TRACKING_STATE_NOT_STARTED;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements AuthTokenDownloaderInterface {


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private WaitWheel mWaitWheel;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (prefs.getString("auth_token", null) != null && prefs.getInt("user_id", 0) != 0) {
            //TODO добавить проверку правильности токена (возможно, новый метод на стороне сервиса)
            redirectToMainActivity();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mSignUpButton = (Button) findViewById(R.id.btn_sign_up);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToSignUp();
            }
        });

        Button forgotPasswordBtn = (Button) findViewById(R.id.btn_forgot_password);
        forgotPasswordBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToResetPassword();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mWaitWheel = new WaitWheel((FrameLayout) findViewById(R.id.waitWheel), this);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_empty_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {

            focusView.requestFocus();
        } else {
            mWaitWheel.showWaitAnimation();

            AuthTokenDownloader loader = new AuthTokenDownloader(this);
            loader.downloadAuthToken(email, password);

        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }


    @Override
    public void onAuthTokenDownloaded(AuthToken auth_token) {
        mWaitWheel.hideWaitAnimation();

        // TODO мб вынести работу с хранилищем в отдельный класс?
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_id", auth_token.user_id);
        editor.putString("auth_token", auth_token.auth_token);
        editor.commit();

        redirectToMainActivity();
    }

    @Override
    public void onAuthTokenDownloadError(String error) {
        mWaitWheel.hideWaitAnimation();
        mPasswordView.setError(getString(R.string.error_incorrect_password));
        mPasswordView.requestFocus();
    }

    private void redirectToMainActivity() {

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Intent intent;

        if (prefs.contains("trackingState")) {
            int trackingState = prefs.getInt("trackingState", TRACKING_STATE_NOT_STARTED);

            if (trackingState == TRACKING_STATE_ACTIVE) {
                intent = new Intent(getApplicationContext(), RouteNewActivity.class);

            } else if (trackingState == TRACKING_STATE_FINISHED) {
                intent = new Intent(getApplicationContext(), RouteSaveActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), RoutesListActivity.class);
            }
        } else {
            intent = new Intent(getApplicationContext(), RoutesListActivity.class);
        }

        startActivity(intent);

    }

    private void redirectToSignUp() {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
    }

    private void redirectToResetPassword() {
        Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
        startActivity(intent);
    }



}

