package com.lanjing.translater.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class TranslateParams implements Parcelable {
    private String q;
    private String from;
    private String to;
    private String appid;
    private String salt;
    private String sign;

    public TranslateParams() {
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

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

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public TranslateParams(String q, String from, String to, String appid, String salt, String sign) {
        this.q = q;
        this.from = from;
        this.to = to;
        this.appid = appid;
        this.salt = salt;
        this.sign = sign;
    }

    protected TranslateParams(Parcel in) {
        q = in.readString();
        from = in.readString();
        to = in.readString();
        appid = in.readString();
        salt = in.readString();
        sign = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(q);
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(appid);
        dest.writeString(salt);
        dest.writeString(sign);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TranslateParams> CREATOR = new Creator<TranslateParams>() {
        @Override
        public TranslateParams createFromParcel(Parcel in) {
            return new TranslateParams(in);
        }

        @Override
        public TranslateParams[] newArray(int size) {
            return new TranslateParams[size];
        }
    };
}
