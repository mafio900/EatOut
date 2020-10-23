package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class PrivateMessagesModel implements Parcelable {
    private String privateMessageID;
    private List<String> users = new ArrayList<>();
    private Timestamp timestamp;

    public PrivateMessagesModel(String privateMessageID, List<String> users, Timestamp timestamp){
        this.privateMessageID = privateMessageID;
        this.users = users;
        this.timestamp = timestamp;
    }

    public PrivateMessagesModel(){}


    protected PrivateMessagesModel(Parcel in) {
        privateMessageID = in.readString();
        users = in.createStringArrayList();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(privateMessageID);
        dest.writeStringList(users);
        dest.writeParcelable(timestamp, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PrivateMessagesModel> CREATOR = new Creator<PrivateMessagesModel>() {
        @Override
        public PrivateMessagesModel createFromParcel(Parcel in) {
            return new PrivateMessagesModel(in);
        }

        @Override
        public PrivateMessagesModel[] newArray(int size) {
            return new PrivateMessagesModel[size];
        }
    };

    public void setPrivateMessageID(String privateMessageID) {
        this.privateMessageID = privateMessageID;
    }

    public String getPrivateMessageID() {
        return privateMessageID;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsetID(){
        String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(users.get(0).equals(myID)){
            return users.get(1);
        }
        else{
            return users.get(0);
        }
    }
}
