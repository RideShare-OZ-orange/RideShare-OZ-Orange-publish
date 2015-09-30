package com.swen900014.orange.rideshareoz;

import java.io.Serializable;

/**
 * Created by Sangzhuoyang Yu on 9/11/15.
 * Encapsulate user data, user can be either a
 * driver or passenger depending on whether the
 * ride is offed by them
 */
public class User implements Serializable
{
    private String name;
    private String email;
    private int phone;
    private int credit;
    private UserType userType;

    private static User currentUser;

    public User(String name, String email, int phone, int credit, UserType userType)
    {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.credit = credit;
        this.userType = userType;
    }

    public User(String username)
    {
        this.name = username;
    }

    public void rate()
    {

    }

    public String getUsername()
    {
        return name;
    }

    public String getEmail()
    {
        return email;
    }

    public UserType getUserType()
    {
        return userType;
    }

    public int getPhone()
    {
        return phone;
    }

    public int getCredit()
    {
        return credit;
    }

    public static void setCurrentUser(User currentUser)
    {
        if (User.currentUser == null)
        {
            User.currentUser = currentUser;
        }

    }

    public static User getCurrentUser()
    {
        return currentUser;
    }
}
