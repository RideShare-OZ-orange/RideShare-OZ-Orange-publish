package com.swen900014.orange.rideshareoz;

import android.accounts.Account;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.swen900014.orange.rideshareoz.Resources.*;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener
{
    private final static String TAG = "MAIN_Authentication";

    /* Request code used to invoke sign in user interactions. */
    private final static int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private static GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private static boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */


    private boolean mShouldResolve = false;
    private Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize request queue
        MyRequest.getInstance(this.getApplicationContext()).
                getRequestQueue();

        this.savedInstanceState = savedInstanceState;

        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        if (id == R.id.action_signout)
        {
            onSignOutClicked();
            return true;
        }
        if (id == R.id.action_OfferRide)
        {
            Intent offerRide = new Intent(this, OfferRide.class);
            offerRide.putExtra(Intent.EXTRA_TEXT, "offer");
            startActivity(offerRide);
            return true;
        }
        if (id == R.id.action_FindRide)
        {
            Intent findRideIntent = new Intent(this, OfferRide.class);
            findRideIntent.putExtra("type", "find");
            startActivity(findRideIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve)
        {
            if (connectionResult.hasResolution())
            {
                try
                {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e)
                {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            }
            else
            {
                // Could not resolve the connection result, show the user an
                // error dialog.
                //showErrorDialog(connectionResult);
            }
        }
        else
        {
            // Show the signed-out UI
            showSignedOutUI();
        }
    }

    private void showSignedOutUI()
    {
        //Intent authentication = new Intent(this, AuthenticationActivity.class);
        //startActivity(authentication);
        setContentView(R.layout.activity_login);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    // Button event for the Offer ride button
    // by Fallie
    public void offerRide(View v)
    {
        Intent offerRide = new Intent(this, OfferRide.class);
        startActivity(offerRide);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.sign_in_button)
        {
            onSignInClicked();
        }
        if (v.getId() == R.id.sign_out_button)
        {
            onSignOutClicked();
        }
    }

    private void onTestQueryClicked(String url)
    {
        new SendURL().execute(url);
    }

    public static void signOut()
    {
        if (mGoogleApiClient.isConnected())
        {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    private void onSignOutClicked()
    {
        if (mGoogleApiClient.isConnected())
        {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }

        showSignedOutUI();
    }

    private void onSignInClicked()
    {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();


        TextView mStatusTextView = (TextView) findViewById(R.id.sign_in_text);
        // Show a message to the user that we are signing in.
        mStatusTextView.setText("Signing in..."/*R.string.signing_in*/);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN)
        {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK)
            {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;

        // Show the signed-in UI
        showSignedInUI();

    }

    private void showSignedInUI()
    {

        //Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        //set current user
        String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        User.setCurrentUser(new User(account.name));


        setContentView(R.layout.activity_myrides);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, (new MyRidesFragment()))
                    .commit();
        }

        new GetUserIDTask().execute();
    }

    public static GoogleApiClient getUserGoogleApiClient()
    {
        return mGoogleApiClient;
    }

    public String getAuthToken()
    {
        //GoogleApiClient mGoogleApiClient = (GoogleApiClient)params[0];
        String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String scopes = "audience:server:client_id:" + SERVER_CLIENT_ID; // Not the app's client ID.

        try
        {
            return GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
        } catch (IOException e)
        {
            Log.e(TAG, "Error retrieving ID token.", e);
            return null;
        } catch (GoogleAuthException e)
        {
            Log.e(TAG, "Error retrieving ID token.", e);
            return null;
        }
    }

    /**
     * Created by George on 29/08/2015.
     */
    public class GetUserIDTask extends AsyncTask<Void, Void, String>
    {
        private final String TAG = "SendID";

        public String getToken()
        {
            return token;
        }

        private String token = null;

        @Override
        protected String doInBackground(Void... params)
        {
            return getAuthToken();
        }

        @Override
        protected void onPostExecute(String result)
        {
            Log.i(TAG, "ID token: " + result);
            if (result != null)
            {
                // Successfully retrieved ID Token
                token = result;

                new SendUserID().execute(result);
            }
            else
            {
                // There was some error getting the ID Token
                // ...
            }
        }
    }

    public class SendURL extends AsyncTask<String, Void, String>
    {

        final String LogTag = "SendURL";

        @Override
        protected String doInBackground(String... params)
        {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String ridesJason = null;

            try
            {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                String urlAddress;

                if (params[0].endsWith("http://144.6.226.237/ride/create?driverid="))
                {
                    urlAddress = params[0] + getAuthToken();
                }
                else
                {
                    urlAddress = params[0];
                }
                URL url = new URL(urlAddress);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null)
                {
                    // Nothing to do.
                    Log.e(LogTag, "input Stream empty");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0)
                {
                    // Stream was empty.  No point in parsing.
                    Log.e(LogTag, "Buffer empty");
                    return null;
                }
                ridesJason = buffer.toString();
                //Log.e(LogTag, ridesJason );
            } catch (IOException e)
            {
                Log.e(LogTag, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    } catch (final IOException e)
                    {
                        Log.e(LogTag, "Error closing stream", e);
                    }
                }
            }

            return ridesJason;
        }

        @Override
        protected void onPostExecute(String result)
        {

            if (result != null)
            {
                // Successfully retrieved rides
                Log.d(LogTag, result);
            }
            else
            {
                // No Rides :(
                Log.e(LogTag, "no rides");
            }
        }
    }

    public class SendUserID extends AsyncTask<String, Void, String>
    {

        private final String TAG = "SendID";

        @Override
        protected String doInBackground(String... params)
        {
            String token = params[0];
            URL url = null;
            try
            {
                url = new URL(LOGIN_URL);
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            }

            HttpURLConnection urlConnection;
            String response = "";
            try
            {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                //urlConnection.connect();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

                writer.write("token=" + token);
                writer.flush();
                writer.close();
                os.close();

                //urlConnection.connect();


                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((line = br.readLine()) != null)
                    {
                        response += line;
                    }
                }
                else
                {
                    response = "";

                    //throw new HttpException(responseCode+"");
                }
                return response;
            } catch (IOException e)
            {
                e.printStackTrace();
                return "fail";
            } catch (Exception e)
            {
                e.printStackTrace();
                return "fail";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            Log.d(TAG, "ID token: " + result);
            if (result != null)
            {
                // Successfully retrieved ID Token
            }
            else
            {
                // There was some error getting the ID Token
                // ...
            }
        }
    }
}
