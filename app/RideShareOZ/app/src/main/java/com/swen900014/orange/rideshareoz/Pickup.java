package com.swen900014.orange.rideshareoz;


import java.io.Serializable;

/**
 * Created by George on 16/09/2015.
 * It is used in the join request, where each
 * passenger is bound with a pickup location
 */
public class Pickup implements Serializable
{
    private User user;
    private Location location;

    public Pickup(User user, Location location)
    {
        this.user = user;
        this.location = location;
    }

    public Location getLocation()
    {
        return location;
    }

    public User getUser()
    {
        return user;
    }
}
