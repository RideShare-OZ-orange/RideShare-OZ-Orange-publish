package com.swen900014.orange.rideshareoz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by George on 15/09/2015.
 * Adapter attached to the list view in
 * MyRides activity, displaying all rides
 * info retrieved from the server
 */
public class RidesAdaptor extends ArrayAdapter<Ride>
{

    public RidesAdaptor(Context context, ArrayList<Ride> rides)
    {
        super(context, 0, rides);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Get the data item for this position
        Ride ride = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_rides_linear, parent, false);
        }

        // Lookup view for data population
        TextView tvType = (TextView) convertView.findViewById(R.id.textViewType);
        TextView tvFromTo = (TextView) convertView.findViewById(R.id.textViewFromTo);
        TextView tvRequests = (TextView) convertView.findViewById(R.id.textViewRequests);
        TextView tvJoins = (TextView) convertView.findViewById(R.id.textViewJoins);
        TextView tvDate = (TextView) convertView.findViewById(R.id.textViewDate);

        // Populate the data into the template view using the data object
        tvType.setText(ride.getRideState().toString() + " ");
        tvFromTo.setText(ride.getStart().getDisplayName() + " To " + ride.getEnd().getDisplayName());
        tvRequests.setText(" " + Integer.toString(ride.getWaiting().size()));
        tvJoins.setText(Integer.toString(ride.getJoined().size()) + " ");
        tvDate.setText(ride.getArrivingTime());
        // Return the completed view to render on screen
        return convertView;
    }
}
