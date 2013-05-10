package com.example.android.rssreader;

/**
 * Simple struct class to hold the data for one rss item --
 * title, link, description.
 */
public class RssItem  {
    private CharSequence mTitle;
    private CharSequence mLink;
    private CharSequence mDescription;
    
    public RssItem() {
        mTitle = "";
        mLink = "";
        mDescription = "";
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

