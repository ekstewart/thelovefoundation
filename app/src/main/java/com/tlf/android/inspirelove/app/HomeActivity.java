package com.tlf.android.inspirelove.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class HomeActivity extends ActionBarActivity {
    private final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new QuotesFragment())
                    .commit();
        }
    }
}
