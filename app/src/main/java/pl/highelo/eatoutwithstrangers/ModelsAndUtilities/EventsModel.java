package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ScrollView;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class EventsModel implements Parcelable {
    private String itemID;
    private String placeAddress;
    private String theme;
    private String description;
    private int maxPeople;
    private String userID;
    private Timestamp timeStamp;
    private double l;
    private List<String> members = new ArrayList<>();
    private List<String> requests = new ArrayList<>();

    public EventsModel(){}

    public EventsModel(String itemID, String placeAddress, String theme, String description, int maxPeople, String userID, Timestamp timeStamp, double l, List<String> members, List<String> requests) {
        this.itemID = itemID;
        this.description = description;
        this.placeAddress = placeAddress;
        this.theme = theme;
        this.maxPeople = maxPeople;
        this.userID = userID;
        this.timeStamp = timeStamp;
        this.l = l;
        this.members = members;
        this.requests = requests;
    }

    protected EventsModel(Parcel in) {
        itemID = in.readString();
        description = in.readString();
        placeAddress = in.readString();
        theme = in.readString();
        maxPeople = in.readInt();
        userID = in.readString();
        l = in.readDouble();
        timeStamp = new Timestamp(in.readLong(), 0);
        in.readStringList(members);
        in.readStringList(requests);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemID);
        dest.writeString(description);
        dest.writeString(placeAddress);
        dest.writeString(theme);
        dest.writeInt(maxPeople);
        dest.writeString(userID);
        dest.writeDouble(l);
        dest.writeLong(timeStamp.getSeconds());
        dest.writeStringList(members);
        dest.writeStringList(requests);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public double getL() {
        return l;
    }

    public void setL(double l) {
        this.l = l;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getRequests() {
        return requests;
    }

    public void setRequests(List<String> requests) {
        this.requests = requests;
    }
}
