package net.xxtri.booktracker;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;


public class RetrieveAccessTokenActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "RetrieveAccessToken";
    private static final int REQ_SIGN_IN_REQUIRED = 55664;

    private String mAccountName;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        findViewById(R.id.button_token).setOnClickListener(this);


        Bundle bundle = getIntent().getExtras();
        String mail = bundle.getString("token");
        // Manual integration? Pop an account chooser to get this:
        mAccountName = mail;
        // Or if you have a GoogleApiClient connected:
        // mAccountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_token) {
            new RetrieveTokenTask().execute(mAccountName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_SIGN_IN_REQUIRED && resultCode == RESULT_OK) {
            // We had to sign in - now we can finish off the token request.
            //mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            new RetrieveTokenTask().execute(mAccountName);

        }


    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        private final static String BOOKS_API_SCOPE
                = "https://www.googleapis.com/auth/books";
        private final static String GPLUS_SCOPE
                = "https://www.googleapis.com/auth/plus.login";
        private final static String mScopes
                = "oauth2:" + BOOKS_API_SCOPE + " " + GPLUS_SCOPE;


        @Override
        protected String doInBackground(String... params) {

            String token = null;
            try {
                token = fetchToken();
                if (token != null) {
                    // **Insert the good stuff here.**
                    // Use the token to access the user's Google data.


                }
            } catch (IOException e) {
                // The fetchToken() method handles Google-specific exceptions,
                // so this indicates something went wrong at a higher level.
                // TIP: Check for network connectivity before starting the AsyncTask.

            }
            return token;


            /**
             String accountName = params[0];
             //String scopes = "oauth2:profile email";
             String token = null;
             try {
             token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, mScopes);
             } catch (IOException e) {
             Log.e(TAG, e.getMessage());
             } catch (UserRecoverableAuthException e) {
             startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
             } catch (GoogleAuthException e) {
             Log.e(TAG, e.getMessage());
             }
             return token;
             **/
        }


        protected String fetchToken(String... params) throws IOException {
            String accountName = params[0];
            //String scopes = "oauth2:profile email";

            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, mScopes);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }
            return token;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //((TextView) findViewById(R.id.token_value)).setText("Token Value: " + s);
        }
    }
}