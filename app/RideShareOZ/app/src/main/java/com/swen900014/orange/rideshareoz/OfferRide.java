package com.swen900014.orange.rideshareoz;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.swen900014.orange.rideshareoz.Resources.*;


/**
 * Created by Qianwen Zhang on 9/12/15.
 * The view activity where users are able to
 * offer a ride as a driver
 */
public class OfferRide extends FragmentActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener
{
    private final static String TAG = "OfferRide";
    private String temp1 = "";
    private String SeatNo = "1";
    private String EditStartTime = "";
    private String EditEndTime = "";

    private String hours = "";
    private String mins = "";
    private String houra = "";
    private String mina = "";

    private CheckBox Check1, Check2;

    private AutoCompleteTextView EditStart;
    private AutoCompleteTextView EditEnd;

    private Button btnSubmit;

    // Coordinates of start and end addresses
    private String latS = "";
    private String lonS = "";
    private String latE = "";
    private String lonE = "";

    private String startAddress = "";
    private String endAddress = "";

    // Determine whether this class is
    // used for offering a ride or searching a
    // ride
    private boolean isFind = false;

    protected GoogleApiClient mGoogleApiClient;

    private Calendar calendar = Calendar.getInstance();
    private TextView displayDate, displayStartTime, displayArrivalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offerride);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        btnSubmit = (Button) findViewById(R.id.button1);
        Button btnReset = (Button) findViewById(R.id.button2);
        Button btnDate = (Button) findViewById(R.id.setDateButton);
        Button btnStartTime = (Button) findViewById(R.id.setStartTimeButton);
        Button btnArrivalTime = (Button) findViewById(R.id.setEndTimeButton);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        displayDate = (TextView) findViewById(R.id.displayDate);
        displayStartTime = (TextView) findViewById(R.id.displayStartTime);
        displayArrivalTime = (TextView) findViewById(R.id.displayArrivalTime);

        TextView textSN = (TextView) findViewById(R.id.txtSeatNo);
        Check1 = (CheckBox) findViewById(R.id.current1);
        Check2 = (CheckBox) findViewById(R.id.current2);

        EditStart = (AutoCompleteTextView)
                findViewById(R.id.Start);

        EditEnd = (AutoCompleteTextView)
                findViewById(R.id.End);

        //auto-complete adapter
        PlaceAutoCompleteAdapter adapterS = new PlaceAutoCompleteAdapter(this,
                android.R.layout.simple_expandable_list_item_1, mGoogleApiClient,
                BOUNDS_GREATER_MELBOURNE, null, EditStart);
        EditStart.setAdapter(adapterS);

        PlaceAutoCompleteAdapter adapterE = new PlaceAutoCompleteAdapter(this,
                android.R.layout.simple_expandable_list_item_1, mGoogleApiClient,
                BOUNDS_GREATER_MELBOURNE, null, EditEnd);
        EditEnd.setAdapter(adapterE);

        //spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.seats, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                SeatNo = String.valueOf(position + 1);

                if (position > 0)
                {
                    Toast.makeText(getBaseContext(), parent.getItemAtPosition(position) + " selected", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        /*  Check if the current state is offering a ride or
          * searching a ride */
        Intent intent = this.getIntent();

        if (intent != null && intent.hasExtra("type"))
        {
            String type = intent.getStringExtra("type");

            if (type.equals("find"))
            {
                textSN.setVisibility(View.GONE);
                btnStartTime.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                isFind = true;
            }
        }

        btnSubmit.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnDate.setOnClickListener(this);
        btnStartTime.setOnClickListener(this);
        btnArrivalTime.setOnClickListener(this);

        getIntent();
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.button2)
        {
            EditStart.setText("");
            EditEnd.setText("");
            displayDate.setText("");
            displayStartTime.setText("");
            displayArrivalTime.setText("");
        }

        if (v.getId() == R.id.button1)
        {
            offerRide(v);

        }
        if (v.getId() == R.id.setDateButton)
        {
            setDate(v);
        }

        if (v.getId() == R.id.setStartTimeButton)
        {
            setStartTime(v);
        }

        if (v.getId() == R.id.setEndTimeButton)
        {
            setArrivalTime(v);
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    public void sendRequest(final Activity activity)
    {
        String startAddressToGoogle = startAddress.replaceAll(" ", "+");

        final String url = "https://maps.googleapis.com/maps/api/geocode/json?" +
                "address=" + startAddressToGoogle +
                "key=AIzaSyBhEI1X-PMslBS2Ggq35bOncxT05mWO9bs";

        final StringRequest getStartLocRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    public void onResponse(String response)
                    {
                        try
                        {
                            JSONObject jsonResponse = new JSONObject(response);
                            System.out.println(jsonResponse.toString());

                            latS = jsonResponse.getJSONArray("results").getJSONObject(0).
                                    getJSONObject("geometry").getJSONObject("location").
                                    getString("lat");
                            lonS = jsonResponse.getJSONArray("results").getJSONObject(0).
                                    getJSONObject("geometry").getJSONObject("location").
                                    getString("lng");

                            // Check response whether it's accurate, if not remind user

                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        getEndpointLoc(activity);
                    }
                },
                new Response.ErrorListener()
                {
                    public void onErrorResponse(VolleyError volleyError)
                    {
                        volleyError.printStackTrace();

                        Toast.makeText(activity, "Invalid address",
                                Toast.LENGTH_SHORT).show();

                        System.out.println("it doesn't work");
                    }
                });

        MyRequest.getInstance(activity).addToRequestQueue(getStartLocRequest);
    }

    private void getEndpointLoc(final Activity activity)
    {
        String endAddressToGoogle = endAddress.replaceAll(" ", "+");

        final String url = "https://maps.googleapis.com/maps/api/geocode/json?" +
                "address=" + endAddressToGoogle +
                "key=AIzaSyBhEI1X-PMslBS2Ggq35bOncxT05mWO9bs";

        final StringRequest getEndLocRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    public void onResponse(String response)
                    {
                        try
                        {
                            JSONObject jsonResponse = new JSONObject(response);
                            System.out.println(jsonResponse.toString());

                            latE = jsonResponse.getJSONArray("results").getJSONObject(0).
                                    getJSONObject("geometry").getJSONObject("location").
                                    getString("lat");
                            lonE = jsonResponse.getJSONArray("results").getJSONObject(0).
                                    getJSONObject("geometry").getJSONObject("location").
                                    getString("lng");

                            // Check response whether it's accurate, if not remind user

                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        /* check if it offer or find  */
                        if (!isFind)
                        {
                            sendRideInfo(activity);
                        }
                        else
                        {
                            Intent searchResultsIntent = new Intent(OfferRide.this, MyRidesActivity.class);
                            searchResultsIntent.putExtra("type", "find");
                            searchResultsIntent.putExtra("s_lon", lonS);
                            searchResultsIntent.putExtra("s_lat", latS);
                            searchResultsIntent.putExtra("group_id", "55cab5dde81ab31606e4814c");
                            searchResultsIntent.putExtra("e_lon", lonE);
                            searchResultsIntent.putExtra("e_lat", latE);
                            searchResultsIntent.putExtra("arrival_time", EditEndTime);
                            startActivity(searchResultsIntent);
                        }

                        // check response, whether it received
                    }
                },
                new Response.ErrorListener()
                {
                    public void onErrorResponse(VolleyError volleyError)
                    {
                        volleyError.printStackTrace();

                        Toast.makeText(activity, "Invalid address",
                                Toast.LENGTH_SHORT).show();

                        System.out.println("it doesn't work");
                    }
                });

        MyRequest.getInstance(activity).addToRequestQueue(getEndLocRequest);
    }

    private void sendRideInfo(final Activity activity)
    {
        StringRequest OfferRequest = new StringRequest(Request.Method.POST,
                OFFER_RIDE_URL, new Response.Listener<String>()
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
        })
        {
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();

                if (Check1.isChecked())
                {
                    params.put("s_lat", "111");
                    params.put("s_lon", "111");
                    params.put("start_add", "mine");
                }
                else
                {
                    params.put("s_lat", latS);
                    params.put("s_lon", lonS);
                    params.put("start_add", startAddress);
                }

                if (Check2.isChecked())
                {
                    params.put("e_lat", "222");
                    params.put("e_lon", "222");
                    params.put("destination", "mine");
                }
                else
                {
                    params.put("e_lat", latE);
                    params.put("e_lon", lonE);
                    params.put("destination", endAddress);
                }

                params.put("group_id", "55cab5dde81ab31606e4814c");
                params.put("seat", SeatNo);
                params.put("start_time", EditStartTime);
                params.put("arrival_time", EditEndTime);
                params.put("username", User.getCurrentUser().getUsername());

                return params;
            }
        };

        MyRequest.getInstance(activity).addToRequestQueue(OfferRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void offerRide(View view)
    {
        if (inputValid())
        {
            startAddress = EditStart.getText().toString();
            endAddress = EditEnd.getText().toString();

            sendRequest(this);
        }
        else
        {
            Toast.makeText(this, "Please fill in all information",
                    Toast.LENGTH_SHORT).show();

            System.out.println("Invalid input in offerRide");
        }
    }

    private DatePickerDialog.OnDateSetListener listener1 = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            String month;
            String day;

            if (monthOfYear < 9)
            {
                month = "0" + String.valueOf(monthOfYear + 1);
            }
            else
            {
                month = String.valueOf(monthOfYear + 1);
            }

            if (dayOfMonth < 10)
            {
                day = "0" + String.valueOf(dayOfMonth);
            }
            else
            {
                day = String.valueOf(dayOfMonth);
            }

            displayDate.setText(dayOfMonth + "-" + month + "-" + year);
            temp1 = String.valueOf(year) + "-" + month + "-" + day + "T";
        }
    };

    private TimePickerDialog.OnTimeSetListener listener2 = new TimePickerDialog.OnTimeSetListener()
    {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute)
        {
            if (hourOfDay < 10)
            {
                hours = "0" + String.valueOf(hourOfDay);
            }
            else
            {
                hours = String.valueOf(hourOfDay);
            }
            if (minute < 10)
            {
                mins = "0" + String.valueOf(minute);
            }
            else
            {
                mins = String.valueOf(minute);
            }

            displayStartTime.setText(hourOfDay + ":" + minute);
            EditStartTime = temp1 + hours + ":" + mins + ":00.000Z";
        }
    };

    private TimePickerDialog.OnTimeSetListener listener4 = new TimePickerDialog.OnTimeSetListener()
    {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute)
        {
            if (hourOfDay < 10)
            {
                houra = "0" + String.valueOf(hourOfDay);
            }
            else
            {
                houra = String.valueOf(hourOfDay);
            }

            if (minute < 10)
            {
                mina = "0" + String.valueOf(minute);
            }
            else
            {
                mina = String.valueOf(minute);
            }

            displayArrivalTime.setText(hourOfDay + ":" + minute);
            EditEndTime = temp1 + houra + ":" + mina + ":00.000Z";
            checkTime(hours, mins, houra, mina);
        }
    };

    public void checkTime(String hours, String mins, String houra, String mina)
    {
        if (hours.compareTo(houra) == 0)
        {
            if (mins.compareTo(mina) >= 0)
            {
                Toast.makeText(getApplicationContext(), "Arrival time must be later than start time!", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(false);
            }
            else
            {
                btnSubmit.setEnabled(true);
            }
        }
        else if (hours.compareTo(houra) > 0)
        {
            Toast.makeText(getApplicationContext(), "Arrival time must be later than start time!", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(false);
        }
        else
        {
            btnSubmit.setEnabled(true);
        }
    }

    public void setDate(View view)
    {
        new DatePickerDialog(OfferRide.this, listener1, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void setStartTime(View view)
    {
        new TimePickerDialog(OfferRide.this, listener2, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true).show();
    }

    public void setArrivalTime(View view)
    {
        new TimePickerDialog(OfferRide.this, listener4, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true).show();
    }

    //Check whether all information needed for offering a ride
    // has been typed in by user
    public boolean inputValid()
    {
        if (!isFind)
        {
            return !(EditEndTime.isEmpty() || EditStartTime.isEmpty() ||
                    EditStart.getText().toString().isEmpty() ||
                    EditEnd.getText().toString().isEmpty());
        }
        else
        {
            return !(EditEndTime.isEmpty() ||
                    EditStart.getText().toString().isEmpty() ||
                    EditEnd.getText().toString().isEmpty());
        }
    }
}
