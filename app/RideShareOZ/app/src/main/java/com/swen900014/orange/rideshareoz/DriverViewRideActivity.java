package com.swen900014.orange.rideshareoz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.swen900014.orange.rideshareoz.Resources.*;


/**
 * Created by Sangzhuoyang Yu on 9/12/15.
 * It initialize a new activity for the ride
 * from the drivers' view. The driver is able
 * to cancel the ride, accept or reject requests
 * from passengers.
 */
public class DriverViewRideActivity extends AppCompatActivity
{
    private TableLayout passengerList;
    private TableLayout waitingList;
    private Ride ride;
    private Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_view_ride);
        thisActivity = this;

        Intent received = getIntent();
        ride = (Ride) received.getSerializableExtra("SelectedRide");

        // Test accepting and rejecting requests
        //ride.acceptJoin(new Pickup(new User("user1", "email", 123, 0, UserType.PASSENGER),
        //        new Location(0.0, 0.0, "carlton")));

        //ride.addWaiting(new Pickup(new User("user2", "email", 123, 0, UserType.PASSENGER),
        //        new Location(0.0, 0.0, "carlton")));

        TextView startLabel = (TextView) findViewById(R.id.startPointText);
        TextView endLabel = (TextView) findViewById(R.id.endPointText);
        TextView startTimeLabel = (TextView) findViewById(R.id.startTimeText);
        TextView arrivalTimeLabel = (TextView) findViewById(R.id.endTimeText);
        TextView driverText = (TextView) findViewById(R.id.driverText);
        TextView seatsText = (TextView) findViewById(R.id.seatsText);

        passengerList = (TableLayout) findViewById(R.id.passengerList);
        waitingList = (TableLayout) findViewById(R.id.waitingList);

        startLabel.setText(ride.getStart().getAddress());
        endLabel.setText(ride.getEnd().getAddress());
        startTimeLabel.setText(ride.getStartTime());
        arrivalTimeLabel.setText(ride.getArrivingTime());
        driverText.setText(ride.getDriver().getUsername());
        seatsText.setText("" + ride.getSeats());

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
        ArrayList<Pickup> waitingListArray = ride.getWaiting();

        passengerList.removeAllViews();
        waitingList.removeAllViews();

        for (final Pickup lift : joinedList)
        {
            TextView pass = new TextView(this);
            pass.setText("name: " + lift.getUser().getUsername());

            if (ride.getRideState() == Ride.RideState.OFFERING)
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

        for (final Pickup lift : waitingListArray)
        {
            TextView request = new TextView(this);
            request.setText("name: " + lift.getUser().getUsername());

            if (ride.getRideState() == Ride.RideState.OFFERING)
            {
                request.setOnClickListener(new View.OnClickListener()
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

            waitingList.addView(request);
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

    public void cancelRide(View view)
    {
        sendCancelRequest(this);
    }

    public void sendCancelRequest(final Activity activity)
    {
        StringRequest cancelRequest = new StringRequest(Request.Method.POST,
                CANCEL_RIDE_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String s)
            {
                System.out.println("response: " + s);

                // Get back to the my rides page
                Intent intent = new Intent(activity, MyRidesActivity.class);
                activity.startActivity(intent);
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

                // User name and ride id for the ride to be cancelled
                String accountName = User.getCurrentUser().getUsername();
                params.put("username", accountName);
                params.put("ride_id", ride.getRideId());

                return params;
            }
        };

        MyRequest.getInstance(this).addToRequestQueue(cancelRequest);
    }
}
