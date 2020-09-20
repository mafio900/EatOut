package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class ReportsModel implements Parcelable {

    private String reportID;
    private String reportedUser;
    private String reportingUser;
    private String theme;
    private String message;
    private Timestamp timeStamp;

    public ReportsModel(){}

    protected ReportsModel(Parcel in) {
        reportID = in.readString();
        reportedUser = in.readString();
        reportingUser = in.readString();
        theme = in.readString();
        message = in.readString();
        timeStamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reportID);
        dest.writeString(reportedUser);
        dest.writeString(reportingUser);
        dest.writeString(theme);
        dest.writeString(message);
        dest.writeParcelable(timeStamp, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReportsModel> CREATOR = new Creator<ReportsModel>() {
        @Override
        public ReportsModel createFromParcel(Parcel in) {
            return new ReportsModel(in);
        }

        @Override
        public ReportsModel[] newArray(int size) {
            return new ReportsModel[size];
        }
    };

    public String getReportID() {
        return reportID;
    }

    public void setReportID(String reportID) {
        this.reportID = reportID;
    }

    public String getReportedUser() {
        return reportedUser;
    }

    public void setReportedUser(String reportedUser) {
        this.reportedUser = reportedUser;
    }

    public String getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(String reportingUser) {
        this.reportingUser = reportingUser;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }
}