package com.lanjing.translater.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Translate implements Parcelable {
    private String from;
    private String to;
    private List<TransResult> trans_result;

    protected Translate(Parcel in) {
        from = in.readString();
        to = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(from);
        dest.writeString(to);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Translate> CREATOR = new Creator<Translate>() {
        @Override
        public Translate createFromParcel(Parcel in) {
            return new Translate(in);
        }

        @Override
        public Translate[] newArray(int size) {
            return new Translate[size];
        }
    };

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<TransResult> getTrans_result() {
        return trans_result;
    }

    public void setTrans_result(List<TransResult> trans_result) {
        this.trans_result = trans_result;
    }
}
