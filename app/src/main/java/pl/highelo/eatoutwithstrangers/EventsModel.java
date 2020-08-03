package pl.highelo.eatoutwithstrangers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.core.GeoHash;

public class EventsModel implements Parcelable {
    private String itemID;
    private String placeName;
    private String placeAddress;
    private GeoHash placeGeoHash;
    private GeoPoint placeLatLng;
    private String date;
    private String time;
    private String theme;
    private int maxPeople;
    private String userID;
    private boolean isEnded;

    private EventsModel(){}

    public EventsModel(String itemID, String placeName, String placeAddress, GeoPoint placeLatLng, String date, String time, String theme, int maxPeople, String userID, boolean isEnded) {
        this.itemID = itemID;
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.placeLatLng = placeLatLng;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.maxPeople = maxPeople;
        this.userID = userID;
        this.isEnded = isEnded;
    }

    protected EventsModel(Parcel in) {
        itemID = in.readString();
        placeName = in.readString();
        placeAddress = in.readString();
        date = in.readString();
        time = in.readString();
        theme = in.readString();
        maxPeople = in.readInt();
        userID = in.readString();
        isEnded = in.readByte() != 0;
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

    public GeoPoint getPlaceLatLng() {
        return placeLatLng;
    }

    public void setPlaceLatLng(GeoPoint placeLatLng) {
        this.placeLatLng = placeLatLng;
    }

    public GeoHash getPlaceGeoHash() {
        return placeGeoHash;
    }

    public void setPlaceGeoHash(GeoHash placeGeoHash) {
        this.placeGeoHash = placeGeoHash;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public boolean isEnded() {
        return isEnded;
    }

    public void setEnded(boolean ended) {
        isEnded = ended;
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
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(theme);
        dest.writeInt(maxPeople);
        dest.writeString(userID);
        dest.writeByte((byte) (isEnded ? 1 : 0));
    }
}
