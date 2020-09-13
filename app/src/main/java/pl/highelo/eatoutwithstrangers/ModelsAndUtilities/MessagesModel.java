package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class MessagesModel implements Parcelable {
    private String userID;
    private Timestamp time;
    private String message;
    private String name;

    public MessagesModel(String userID, Timestamp time, String message, String name) {
        this.userID = userID;
        this.time = time;
        this.message = message;
        this.name = name;
    }

    public MessagesModel(){}

    protected MessagesModel(Parcel in) {
        userID = in.readString();
        time = in.readParcelable(Timestamp.class.getClassLoader());
        message = in.readString();
    }

    public static final Creator<MessagesModel> CREATOR = new Creator<MessagesModel>() {
        @Override
        public MessagesModel createFromParcel(Parcel in) {
            return new MessagesModel(in);
        }

        @Override
        public MessagesModel[] newArray(int size) {
            return new MessagesModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userID);
        parcel.writeParcelable(time, i);
        parcel.writeString(message);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
