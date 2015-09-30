package com.swen900014.orange.rideshareoz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import static com.swen900014.orange.rideshareoz.Resources.*;


/**
 * Created by Sangzhuoyang Yu on 9/12/15.
 * The view activity where user information are
 * displayed
 */
public class UserInfoActivity extends AppCompatActivity
{
    private Ride ride;
    private Pickup userInfo;
    private Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Intent intent = getIntent();
        ride = (Ride) intent.getSerializableExtra("Ride");
        Ride.RideState rideState = ride.getRideState();

        if (intent.hasExtra("UserInfo"))
        {
            userInfo = (Pickup) intent.getSerializableExtra("UserInfo");
        }
        else
        {
            userInfo = new Pickup(ride.getDriver(), ride.getEnd());
        }

        thisActivity = this;

        TextView nameText = (TextView) findViewById(R.id.ShowName);
        TextView phoneText = (TextView) findViewById(R.id.ShowPhone);
        TextView emailText = (TextView) findViewById(R.id.ShowEmail);
        TextView creditText = (TextView) findViewById(R.id.ShowCredit);
        TextView departureText = (TextView) findViewById(R.id.ShowDeparture);

        nameText.setText(userInfo.getUser().getUsername());
        phoneText.setText("" + userInfo.getUser().getPhone());
        emailText.setText(userInfo.getUser().getEmail());
        creditText.setText("" + userInfo.getUser().getCredit());
        departureText.setText(userInfo.getLocation().getAddress());

        Button acceptButton = (Button) findViewById(R.id.acceptButton);
        Button rejectButton = (Button) findViewById(R.id.rejectButton);

        // Hide accept and reject options if current user is
        // not driver offering the ride
        if (rideState == Ride.RideState.OFFERING &&
                userInfo.getUser().getUsername() != ride.getDriver().getUsername() &&
                ride.hasRequest(userInfo.getUser()))
        {
            acceptButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sendAcceptRequest(userInfo, ride, thisActivity);
                }
            });
            rejectButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sendRejectRequest(ride, userInfo, thisActivity);
                }
            });
        }
        else
        {
            acceptButton.setVisibility(View.GONE);
            rejectButton.setVisibility(View.GONE);
        }
    }

    public void sendAcceptRequest(final Pickup lift, final Ride ride,
                                  final Activity activity)
    {
        StringRequest acceptRequest = new StringRequest(Request.Method.POST,
                ACCEPT_REQUEST_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String s)
            {
                System.out.println("response: " + s);

                ride.acceptJoin(lift);

                Intent intent = new Intent(thisActivity, DriverViewRideActivity.class);
                intent.putExtra("SelectedRide", ride);

                activity.startActivity(intent);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                volleyError.printStackTrace();

                System.out.println("Sending accept failed!");
            }
        }){
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();

                params.put("username", lift.getUser().getUsername());
                params.put("ride_id", ride.getRideId());

                return params;
            }
        };

        MyRequest.getInstance(activity).addToRequestQueue(acceptRequest);
    }

    public void sendRejectRequest(final Ride ride, final Pickup lift,
                                  final Activity activity)
    {
        StringRequest rejectRequest = new StringRequest(Request.Method.POST,
                REJECT_REQUEST_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String s)
            {
                System.out.println("response: " + s);

                ride.rejectJoin(userInfo);
                Intent intent = new Intent(activity, DriverViewRideActivity.class);
                intent.putExtra("SelectedRide", ride);

                activity.startActivity(intent);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                volleyError.printStackTrace();

                System.out.println("Sending reject failed!");
            }
        }){
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();

                params.put("username", lift.getUser().getUsername());
                params.put("ride_id", ride.getRideId());

                return params;
            }
        };

        MyRequest.getInstance(activity).addToRequestQueue(rejectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
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
}
