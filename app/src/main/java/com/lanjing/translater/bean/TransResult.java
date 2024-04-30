package com.lanjing.translater.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class TransResult implements Parcelable {
    private String src;
    private String dst;
    private int error_code;

    protected TransResult(Parcel in) {
        src = in.readString();
        dst = in.readString();
        error_code = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(src);
        dest.writeString(dst);
        dest.writeInt(error_code);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransResult> CREATOR = new Creator<TransResult>() {
        @Override
        public TransResult createFromParcel(Parcel in) {
            return new TransResult(in);
        }

        @Override
        public TransResult[] newArray(int size) {
            return new TransResult[size];
        }
    };

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }
}
