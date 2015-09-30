package com.swen900014.orange.rideshareoz;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.swen900014.orange.rideshareoz.Resources.*;


/**
 * Created by Sangzhuoyang Yu on 9/12/15.
 * It initialize a new activity for the ride
 * from the normal users' view. Users are able
 * to send join request and leave request of the ride
 * to the server.
 */
public class PassViewRideActivity extends FragmentActivity
        implements GoogleApiClient.OnConnectionFailedListener
{
    private final static String TAG = "Passenger View Ride";

    private String lat = "";
    private String lon = "";
    private String address = "";

    protected GoogleApiClient mGoogleApiClient;
    private AutoCompleteTextView pickUpLocText;

    private TableLayout passengerList;
    private Ride ride;

    private Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_view_ride);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();
        //mGoogleApiClient.connect();

        thisActivity = this;

        ride = (Ride) getIntent().getSerializableExtra("SelectedRide");

        TextView startLabel = (TextView) findViewById(R.id.startEditPass);
        TextView endLabel = (TextView) findViewById(R.id.endEditPass);
        TextView startTimeLabel = (TextView) findViewById(R.id.startTimeEditPass);
        TextView arrivalTimeLabel = (TextView) findViewById(R.id.arrivalTimeEditPass);
        TextView driverText = (TextView) findViewById(R.id.driverTextPassView);

        TextView inputTabelName = (TextView) findViewById(R.id.inputTableName);
        TextView seatsText = (TextView) findViewById(R.id.seatsEditPass);

        // Set up auto-place-complete text view
        pickUpLocText = (AutoCompleteTextView) findViewById(R.id.pickUpLocText);
        passengerList = (TableLayout) findViewById(R.id.passengerListPass);

        pickUpLocText = (AutoCompleteTextView)
                findViewById(R.id.pickUpLocText);

        PlaceAutoCompleteAdapter adapter = new PlaceAutoCompleteAdapter(this,
                android.R.layout.simple_expandable_list_item_1, mGoogleApiClient,
                BOUNDS_GREATER_MELBOURNE, null, pickUpLocText);
        //pickUpLocText.setOnItemClickListener(mAutoCompleteClickListener);
        pickUpLocText.setAdapter(adapter);

        Button joinLeaveButton = (Button) findViewById(R.id.joinButton);

        // Display views based on the state of the ride
        if (ride.getRideState() == Ride.RideState.VIEWING)
        {
            inputTabelName.setVisibility(View.GONE);
            pickUpLocText.setVisibility(View.GONE);
            joinLeaveButton.setVisibility(View.GONE);
        }
        else if (ride.getRideState() == Ride.RideState.JOINED)
        {
            joinLeaveButton.setText(getString(R.string.LeaveRide));

            pickUpLocText.setVisibility(View.GONE);
            inputTabelName.setVisibility(View.GONE);
        }
        else
        {
            joinLeaveButton.setText(getString(R.string.joinButton));
        }

        startLabel.setText(ride.getStart().getAddress());
        endLabel.setText(ride.getEnd().getAddress());
        startTimeLabel.setText(ride.getStartTime());
        arrivalTimeLabel.setText(ride.getArrivingTime());
        driverText.setText(ride.getDriver().getUsername());
        seatsText.setText("" + ride.getSeats());

        // Currently there is no pick up time of passengers
        TextView pickupTimeLabel = (TextView) findViewById(R.id.pickuptimeLabel);
        pickupTimeLabel.setVisibility(View.GONE);
        TextView pickupTimeText = (TextView) findViewById(R.id.pickupTimeText);
        pickupTimeText.setVisibility(View.GONE);

        driverText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(thisActivity, UserInfoActivity.class);
                intent.putExtra("Ride", ride);

                thisActivity.startActivity(intent);
            }
        });

        updateView();
    }

    public void updateView()
    {
        ArrayList<Pickup> joinedList = ride.getJoined();

        passengerList.removeAllViews();

        for (final Pickup lift : joinedList)
        {
            TextView pass = new TextView(this);
            pass.setText("name: " + lift.getUser().getUsername());

            if (ride.getRideState() == Ride.RideState.JOINED)
            {
                pass.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(thisActivity, UserInfoActivity.class);
                        intent.putExtra("Ride", ride);
                        intent.putExtra("UserInfo", lift);
                        thisActivity.startActivity(intent);
                    }
                });
            }

            passengerList.addView(pass);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pass_view_ride, menu);
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

        return super.onOptionsItemSelected(item);
    }

    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this, "Could not connect to Google API Client: Error " +
                        connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }

    public void onClick(View view)
    {
        if (ride.getRideState() == Ride.RideState.NEW)
        {
            if (inputValid())
            {
                address = pickUpLocText.getText().toString();

                sendRequest(this);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Please fill in the address",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (ride.getRideState() == Ride.RideState.JOINED)
        {
            leaveRide(this);
        }

        updateView();
    }

    public void leaveRide(final Activity activity)
    {
        StringRequest leaveRequest = new StringRequest(Request.Method.POST,
                LEAVE_RIDE_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String s)
            {
                System.out.println("response: " + s);

                // Get back to the my rides page
                activity.finish();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                volleyError.printStackTrace();
                System.out.println("Sending post failed!");
            }
        }){
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();

                // User name and ride id
                params.put("username", User.getCurrentUser().getUsername());
                params.put("ride_id", ride.getRideId());

                return params;
            }
        };

        MyRequest.getInstance(this).addToRequestQueue(leaveRequest);
    }

    public void sendRequest(final Activity activity)
    {
        String addressToGoogle = address.replaceAll(" ", "+");

        String url = "https://maps.googleapis.com/maps/api/geocode/json?" +
                "address=" + addressToGoogle + "key=AIzaSyBhEI1X-PMslBS2Ggq35bOncxT05mWO9bs";

        StringRequest getLocRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    public void onResponse(String response)
                    {
                        try
                        {
                            JSONObject jsonResponse = new JSONObject(response);
                            System.out.println(jsonResponse.toString());

                            lat = jsonResponse.getJSONArray("results").getJSONObject(0).
                                    getJSONObject("geometry").getJSONObject("location").
                                    getString("lat");
                            lon = jsonResponse.getJSONArray("results").getJSONObject(0).
                                    getJSONObject("geometry").getJSONObject("location").
                                    getString("lng");

                            // Check response whether it's accurate, if not remind user

                            System.out.println("s" + response);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        sendJoinRequest(activity);

                        // check response, whether it received
                    }
                },
                new Response.ErrorListener()
                {
                    public void onErrorResponse(VolleyError volleyError)
                    {
                        volleyError.printStackTrace();
                        System.out.println("it doesn't work");
                    }
                });

        MyRequest.getInstance(activity).addToRequestQueue(getLocRequest);
    }

    private void sendJoinRequest(final Activity activity)
    {
        StringRequest joinRequest = new StringRequest(Request.Method.POST,
                JOIN_REQUEST_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String s)
            {
                System.out.println("response: " + s);

                activity.finish();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                volleyError.printStackTrace();

                System.out.println("Sending post failed!");
            }
        }){
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();

                /*address = stateText.getText().toString() + ", " +
                        suburbText.getText().toString() + ", " +
                        streetText.getText().toString();
                */

                params.put("username", User.getCurrentUser().getUsername());
                params.put("group_id", "55cab5dde81ab31606e4814c");
                params.put("ride_id", ride.getRideId());
                params.put("p_lat", lat);
                params.put("p_lon", lon);
                params.put("pickup_add", address);

                return params;
            }
        };

        MyRequest.getInstance(activity).addToRequestQueue(joinRequest);
    }

    // Check whether user has typed in the pickup location
    public boolean inputValid()
    {
        return !pickUpLocText.getText().toString().isEmpty();
    }
}