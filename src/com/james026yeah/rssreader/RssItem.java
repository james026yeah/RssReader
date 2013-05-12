package com.james026yeah.rssreader;

import android.graphics.Bitmap;

/**
 * Simple struct class to hold the data for one rss item --
 * title, link, description.
 */
public class RssItem  {
	public static final String mIconName = "favicon.ico";
	
    private CharSequence mTitle;        //====>The title of the item. 
    private CharSequence mLink;         //====>The URL of the item. 
    private CharSequence mDescription;  //====>The item synopsis.
    private CharSequence mAuthor;       //====>Email address of the author of the item.
    private CharSequence mImgTitle;
    private String mImgURL;
    
    public RssItem() {
        mTitle = "";
        mLink = "";
        mDescription = "";
        mImgURL = "";
    }
    
    public RssItem(CharSequence title, CharSequence link, CharSequence description) {
        mTitle = title;
        mLink = link;
        mDescription = description;
    }

    public CharSequence getDescription() {
        return mDescription;
    }

    public void setDescription(CharSequence description) {
        mDescription = description;
    }

    public CharSequence getLink() {
        return mLink;
    }

    public void setLink(CharSequence link) {
        mLink = link;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
    }
     
// If we made this class Parcelable, the code would look like...

//    public void writeToParcel(Parcel parcel) {
//        parcel.writeString(mTitle.toString());
//        parcel.writeString(mLink.toString());
//        parcel.writeString(mDescription.toString());
//    }
//    
//    
//    public static Object createFromParcel(Parcel parcel) {
//        return new RssItem(
//                parcel.readString(),
//                parcel.readString(),
//                parcel.readString());
//    }
}

