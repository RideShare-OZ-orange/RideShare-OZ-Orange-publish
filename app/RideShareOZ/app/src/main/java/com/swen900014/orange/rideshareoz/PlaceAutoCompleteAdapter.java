package com.swen900014.orange.rideshareoz;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sangzhuoyang Yu on 9/9/15.
 * The adapter attached to the AutoCompleteView
 * , which auto retrieves predicted location from
 * google server with google place api.
 */
public class PlaceAutoCompleteAdapter extends
        ArrayAdapter<PlaceAutoCompleteAdapter.PlaceAutoComplete> implements Filterable
{
    private static final String TAG = "PlaceAutocomplete";

    private ArrayList<PlaceAutoComplete> mResultList;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds mBounds;
    private AutocompleteFilter mPlaceFilter;
    private Context mContext;
    private AutoCompleteTextView autoCompleteTextView;

    public PlaceAutoCompleteAdapter(Context context, int resource, GoogleApiClient gac,
                                    LatLngBounds bounds, AutocompleteFilter filter,
                                    AutoCompleteTextView textView)
    {
        super(context, resource);

        mContext = context;
        mGoogleApiClient = gac;
        mBounds = bounds;
        mPlaceFilter = filter;
        autoCompleteTextView = textView;
    }

    public void setBounds(LatLngBounds bounds)
    {
        mBounds = bounds;
    }

    /**
     * Returns the number of results received in the last autocomplete query.
     */
    @Override
    public int getCount()
    {
        return mResultList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.auto_place_list, parent,
                false);
        PlaceAutoComplete place = mResultList.get(position);

        final TextView placeView = (TextView) rowView.findViewById(R.id.description);
        placeView.setText(place.toString());
        placeView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                autoCompleteTextView.setText(placeView.getText());
            }
        });

        return rowView;
    }

    @Override
    public Filter getFilter()
    {
        Filter filter = new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults results = new FilterResults();
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null)
                {
                    // Query the autocomplete API for the (constraint) search string.
                    mResultList = getAutoComplete(constraint);
                    if (mResultList != null)
                    {
                        // The API successfully returned results.
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                if (results != null && results.count > 0)
                {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                }
                else
                {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

    /**
     * Returns an item from the last autocomplete query.
     */
    @Override
    public PlaceAutoComplete getItem(int position)
    {
        return mResultList.get(position);
    }

    private ArrayList<PlaceAutoComplete> getAutoComplete(CharSequence query)
    {
        if (mGoogleApiClient.isConnected())
        {
            Log.i(TAG, "Starting autocomplete query for: " + query);

            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient,
                            query.toString(), mBounds, mPlaceFilter);
            AutocompletePredictionBuffer acp = results.await(60, TimeUnit.SECONDS);

            final Status status = acp.getStatus();

            if (!status.isSuccess())
            {
                Toast.makeText(getContext(), "Error contacting API: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());
                acp.release();
                return null;
            }

            Log.i(TAG, "Query completed. received " + acp.getCount() + " predictions");

            Iterator<AutocompletePrediction> iterator = acp.iterator();
            ArrayList resultList = new ArrayList(acp.getCount());

            while (iterator.hasNext())
            {
                AutocompletePrediction prediction = iterator.next();

                resultList.add(new PlaceAutoComplete(prediction.getPlaceId(),
                        prediction.getDescription()));
            }

            acp.release();

            return resultList;
        }

        Log.e(TAG, "Google API client is not connected for autocomplete query.");

        return null;
    }

    class PlaceAutoComplete
    {
        public String placeId;
        public String description;

        PlaceAutoComplete(String placeId, String description)
        {
            this.placeId = placeId;
            this.description = description;
        }

        public String toString()
        {
            return description;
        }
    }
}
