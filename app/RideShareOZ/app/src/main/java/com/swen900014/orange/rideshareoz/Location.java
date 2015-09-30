package com.swen900014.orange.rideshareoz;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.StringTokenizer;


/**
 * Created by Sangzhuoyang Yu & George on 9/11/15.
 * Encapsulate location data.
 */
public class Location implements Serializable
{
    private Double lat;
    private Double lon;
    private String address = "dummy";
    private String suburb;

    /*
    Display name is the suburb unless it is a landmark
     */
    private String displayName;

    private String extractDisplayName(String address)
    {
        String name = "";
        StringTokenizer st = new StringTokenizer(address, ",");
        int count = st.countTokens();

        //get the suburb name
        for (int i = 0; i < count - 2; i++)
        {
            name = st.nextToken();
        }

        return name;
    }

    public Location(Double lat, Double lon, String address)
    {
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.displayName = extractDisplayName(address);
    }

    public Location(Double lat, Double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }

    public void getLocation(final Activity activity)
    {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?" +
                "address=" + address + ",+Australia&" +
                "key=AIzaSyBhEI1X-PMslBS2Ggq35bOncxT05mWO9bs";

        StringRequest getLocRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    public void onResponse(String response)
                    {
                        try
                        {
                            JSONObject jsonResponse = new JSONObject(response);
                            System.out.println(jsonResponse.toString());

                            lat = Double.parseDouble(jsonResponse.getJSONArray("results").getJSONObject(0).
                                    getJSONObject("geometry").getJSONObject("location").
                                    getString("lat"));
                            lon = Double.parseDouble(jsonResponse.getJSONArray("results").getJSONObject(0).
                                    getJSONObject("geometry").getJSONObject("location").
                                    getString("lng"));

                            // Check response whether it's accurate, if not remind user

                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }

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

    public Location(String address)
    {
        this.address = address;
    }

    public void setLat(Double lat)
    {
        this.lat = lat;
    }

    public void setLon(Double lon)
    {
        this.lon = lon;
    }

    public Double getLat()
    {
        return lat;
    }

    public Double getLon()
    {
        return lon;
    }

    public String getSuburb()
    {
        return suburb;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setSuburb(String suburb)
    {
        this.suburb = suburb;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
