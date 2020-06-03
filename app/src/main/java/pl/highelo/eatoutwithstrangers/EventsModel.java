package pl.highelo.eatoutwithstrangers;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.HashMap;

public class EventsModel implements Serializable {
    private String itemID;
    private String placeName;
    private String placeAddress;
    private HashMap<String, Double> placeLatLng;
    private String date;
    private String time;
    private String theme;
    private int maxPeople;
    private String userID;
    private boolean isEnded;

    private EventsModel(){}

    public EventsModel(String itemID, String placeName, String placeAddress, HashMap<String, Double> placeLatLng, String date, String time, String theme, int maxPeople, String userID, boolean isEnded) {
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

    public HashMap<String, Double> getPlaceLatLng() {
        return placeLatLng;
    }

    public void setPlaceLatLng(HashMap<String,Double> placeLatLng) {
        this.placeLatLng = placeLatLng;
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
}
