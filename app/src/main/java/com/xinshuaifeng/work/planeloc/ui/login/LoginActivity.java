package com.xinshuaifeng.work.planeloc.ui.login;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xinshuaifeng.work.planeloc.R;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    SharedPreferences sp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (sp == null) {
            sp = getSharedPreferences("plane", Context.MODE_PRIVATE);
        }
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText planeNoEditText = findViewById(R.id.planeNo);
        final EditText tailNoEditText = findViewById(R.id.tailNo);
        final EditText hostEditText = findViewById(R.id.host);
        planeNoEditText.setText(sp.getString("planeNo", null));
        tailNoEditText.setText(sp.getString("tailNo", null));
        hostEditText.setText(sp.getString("host", null));

        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    planeNoEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    tailNoEditText.setError(getString(loginFormState.getPasswordError()));
                }
                if (loginFormState.getHostError() !=null ) {
                    hostEditText.setError(getString(loginFormState.getHostError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(planeNoEditText.getText().toString(),
                        tailNoEditText.getText().toString(),
                        hostEditText.getText().toString());
            }
        };
        planeNoEditText.addTextChangedListener(afterTextChangedListener);
        tailNoEditText.addTextChangedListener(afterTextChangedListener);
        hostEditText.addTextChangedListener(afterTextChangedListener);

        tailNoEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(planeNoEditText.getText().toString(),
                            tailNoEditText.getText().toString(),
                            hostEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(planeNoEditText.getText().toString(),
                        tailNoEditText.getText().toString(),
                        hostEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String host = sp.getString("host", "192.168.1.1");

        sp.edit().putString("planeNo", model.getPlaneNo())
                .putString("tailNo", model.getTailNo())
                .putString("hostWas", host)
                .putString("host", model.getHost()).apply();
        Toast.makeText(getApplicationContext(), R.string.save_success, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
