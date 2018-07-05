package com.example.johnrobert.manongcustomer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.logging.Logger;

public class ImplementationItem extends ListItem implements Parcelable {
    public final String title;
    public final int imageRes;
    private final String activityClassName;
    public final Service service;
    Logger logger = Logger.getLogger(ImplementationItem.class.getName());

    ImplementationItem(int itemId, String title, int imageRes, Class<?> clazz, Service service) {
        super(ImplementationAdapter.VIEW_TYPE_IMPLEMENTATION, itemId);

        this.title = title;
        this.imageRes = imageRes;
        this.activityClassName = clazz.getName();
        this.service = service;
    }

    protected ImplementationItem(Parcel in) {
        super(in);
        title = in.readString();
        imageRes = in.readInt();
        activityClassName = in.readString();
        service = (Service) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(title);
        dest.writeInt(imageRes);
        dest.writeString(activityClassName);
        dest.writeSerializable(service);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImplementationItem> CREATOR = new Creator<ImplementationItem>() {
        @Override
        public ImplementationItem createFromParcel(Parcel in) {
            return new ImplementationItem(in);
        }

        @Override
        public ImplementationItem[] newArray(int size) {
            return new ImplementationItem[size];
        }
    };

    Class<?> getActivityClass() {
        try {
            return Class.forName(activityClassName);
        } catch (ClassNotFoundException e) {
            e.getCause();
        }

        return null;
    }


}