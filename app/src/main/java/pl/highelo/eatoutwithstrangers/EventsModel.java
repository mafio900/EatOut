package pl.highelo.eatoutwithstrangers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class EventsModel implements Parcelable {
    private String itemID;
    private String placeName;
    private String placeAddress;
    private String theme;
    private int maxPeople;
    private int joinedPeople;
    private String userID;
    private Timestamp timeStamp;

    public EventsModel(){}

    public EventsModel(String itemID, String placeName, String placeAddress, String theme, int maxPeople, String userID, Timestamp timeStamp) {
        this.itemID = itemID;
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.theme = theme;
        this.maxPeople = maxPeople;
        this.userID = userID;
        this.timeStamp = timeStamp;
    }

    protected EventsModel(Parcel in) {
        itemID = in.readString();
        placeName = in.readString();
        placeAddress = in.readString();
        theme = in.readString();
        maxPeople = in.readInt();
        userID = in.readString();
        timeStamp = new Timestamp(in.readLong(), 0);
    }

    public static final Creator<EventsModel> CREATOR = new Creator<EventsModel>() {
        @Override
        public EventsModel createFromParcel(Parcel in) {
            return new EventsModel(in);
        }

        @Override
        public EventsModel[] newArray(int size) {
            return new EventsModel[size];
        }
    };

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(int maxPeople) {
        this.maxPeople = maxPeople;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getJoinedPeople() {
        return joinedPeople;
    }

    public void setJoinedPeople(int joinedPeople) {
        this.joinedPeople = joinedPeople;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemID);
        dest.writeString(placeName);
        dest.writeString(placeAddress);
        dest.writeString(theme);
        dest.writeInt(maxPeople);
        dest.writeString(userID);
        dest.writeLong(timeStamp.getSeconds());
    }
}
