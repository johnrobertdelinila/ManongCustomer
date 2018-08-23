package com.example.johnrobert.manongcustomer;

import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class RequestViewHolder extends RecyclerView.ViewHolder{

    TextView serviceName, quotes, date, booked, compare_cancelled;
    MaterialCardView container_text;

    public RequestViewHolder(@NonNull View itemView) {
        super(itemView);
        serviceName = itemView.findViewById(R.id.text_service_name);
        quotes = itemView.findViewById(R.id.text_service_quotes);
        date = itemView.findViewById(R.id.text_service_date);
        booked = itemView.findViewById(R.id.text_booked_service);
        compare_cancelled = itemView.findViewById(R.id.text_cancelled_compare);
        container_text = itemView.findViewById(R.id.card_cancelled_compare);
    }

}
