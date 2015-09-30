package com.swen900014.orange.rideshareoz;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Created by Sangzhuoyang Yu on 9/12/15.
 * It contains a static request queue
 * storing post and get requests. It is instantiated
 * once the program starts, and destroyed
 * at the end of the whole program lifetime.
 */
public class MyRequest
{
    private static MyRequest mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;

    private MyRequest(Context context)
    {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized MyRequest getInstance(Context context)
    {
        if (mInstance == null)
        {
            mInstance = new MyRequest(context);
        }

        return mInstance;
    }

    public RequestQueue getRequestQueue()
    {
        if (mRequestQueue == null)
        {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }
}
