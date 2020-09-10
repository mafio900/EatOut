package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.os.Parcel;
import android.os.Parcelable;

public class UsersModel implements Parcelable {
    private String userID;
    private String fName;
    private String city;
    private String email;
    private String birthDate;
    private String description;
    private String image;
    private String image_thumbnail;

    public UsersModel(){}

    public UsersModel(String userID, String fName, String city, String email, String birthDate, String description, String image, String image_thumbnail) {
        this.userID = userID;
        this.fName = fName;
        this.city = city;
        this.email = email;
        this.birthDate = birthDate;
        this.description = description;
        this.image = image;
        this.image_thumbnail = image_thumbnail;
    }

    protected UsersModel(Parcel in) {
        userID = in.readString();
        fName = in.readString();
        city = in.readString();
        email = in.readString();
        birthDate = in.readString();
        description = in.readString();
        image = in.readString();
        image_thumbnail = in.readString();
    }

    public static final Creator<UsersModel> CREATOR = new Creator<UsersModel>() {
        @Override
        public UsersModel createFromParcel(Parcel in) {
            return new UsersModel(in);
        }

        @Override
        public UsersModel[] newArray(int size) {
            return new UsersModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userID);
        parcel.writeString(fName);
        parcel.writeString(city);
        parcel.writeString(email);
        parcel.writeString(birthDate);
        parcel.writeString(description);
        parcel.writeString(image);
        parcel.writeString(image_thumbnail);
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_thumbnail() {
        return image_thumbnail;
    }

    public void setImage_thumbnail(String image_thumbnail) {
        this.image_thumbnail = image_thumbnail;
    }
}
