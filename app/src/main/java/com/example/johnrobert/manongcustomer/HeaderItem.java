package com.example.johnrobert.manongcustomer;

class HeaderItem extends ListItem {

    public final String title;

    HeaderItem(int viewType, int itemId, String title) {
        super(viewType, itemId);
        this.title = title;
    }
}
