package com.example.johnrobert.manongcustomer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

class ImplementationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int VIEW_TYPE_HEADER = 0;
    static final int VIEW_TYPE_IMPLEMENTATION = 1;


    private final ArrayList<ListItem> listItems;
    private OnItemClickListener onItemClickListener;
    private int lastAnimatedPosition = -1;
    private boolean lockAnimation = false;

    ImplementationAdapter(Context context, OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        listItems = new ArrayList<>();
//        listItems.add(new HeaderItem(VIEW_TYPE_HEADER, 0, "Services For You"));
        listItems.add(
                new ImplementationItem(
                        4,
                        "Plumbing",
                        R.drawable.plumbing_filipino,
                        ServiceDetailActivity.class,
                        initServicePlumber(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        4,
                        "Laundry Services",
                        R.drawable.laba_filipina,
                        ServiceDetailActivity.class,
                        initServicePlumber(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        4,
                        "Appliance Repair",
                        R.drawable.reapairing_filipino,
                        ServiceDetailActivity.class,
                        initServicePlumber(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        4,
                        "Electrical Wiring",
                        R.drawable.electrician_filipino,
                        ServiceDetailActivity.class,
                        initServiceElectric(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        4,
                        "Household Cleaning",
                        R.drawable.cleaning_filipina,
                        ServiceDetailActivity.class,
                        initServicePlumber(new Service())
                )
        );
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_header, parent, false));
            case VIEW_TYPE_IMPLEMENTATION:
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_implementation, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                ((HeaderViewHolder) holder).bind((HeaderItem) listItems.get(position));
                break;
            case VIEW_TYPE_IMPLEMENTATION:
                ItemViewHolder viewHolder = (ItemViewHolder) holder;
                viewHolder.bind((ImplementationItem) listItems.get(position), onItemClickListener);

                if (position < lastAnimatedPosition) {
                    lockAnimation = true;
                }
                if (!lockAnimation) {
                    lastAnimatedPosition = position;
                    AnimatorUtils.startLoadingAnimation(viewHolder.imageView);
                }
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).viewType;
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }


    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;

        HeaderViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.title);
        }

        void bind(final HeaderItem item) {
            titleView.setText(item.title);
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView titleView;

        ItemViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.implementation_imge);
            titleView = view.findViewById(R.id.implementation_title);
        }

        void bind(final ImplementationItem item, final OnItemClickListener itemClickListener) {
            imageView.setImageResource(item.imageRes);
            titleView.setText(item.title);
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(itemView, item));

        }
    }

    interface OnItemClickListener {
        void onItemClick(View fromView, ImplementationItem item);
    }

    @Override
    public long getItemId(int position) {
        return listItems.get(position).itemId;
    }

    int getItemPosition(final long itemId) {
        for (int position = 0; position < listItems.size(); position++) {
            ListItem listItem = listItems.get(position);
            if (listItem.itemId == itemId) return position;
        }
        return RecyclerView.NO_POSITION;
    }

    private Service initServiceElectric(Service service) {
        ArrayList<String> title = new ArrayList<>();
        title.add("What wiring work do you need?");
        title.add("What's the wiring?");
        title.add("Please describe the electrical work");
        title.add("When do you need it?");

        ArrayList<String> subTitle = new ArrayList<>();
        subTitle.add("");
        subTitle.add("");
        subTitle.add("Optional");
        subTitle.add("");

        ArrayList<Boolean> isInput = new ArrayList<>();
        isInput.add(false);
        isInput.add(true);
        isInput.add(false);
        isInput.add(false);

        ArrayList<Integer> viewTypes = new ArrayList<>();
        viewTypes.add(1);
        viewTypes.add(1);
        viewTypes.add(3);
        viewTypes.add(4);

        ArrayList<ArrayList<String>> answers = new ArrayList<>();

        ArrayList<String> firstTitle = new ArrayList<>();
        firstTitle.add("Install");
        firstTitle.add("Repair / Replace");
        firstTitle.add("Relocate / Move");
        firstTitle.add("Inspection");

        ArrayList<String> secondTitle = new ArrayList<>();
        secondTitle.add("Air conditioner / Water heater");
        secondTitle.add("Fan / Lightning");

        answers.add(firstTitle);
        answers.add(secondTitle);
        answers.add(null);
        answers.add(null);

        service.setServiceName("Electrical Wiring");
        service.setTitle(title);
        service.setSubtitle(subTitle);
        service.setViewTypes(viewTypes);
        service.setIsInput(isInput);
        service.setAnswers(answers);

        return service;
    }

    private Service initServicePlumber(Service service) {

        ArrayList<String> title = new ArrayList<>();
        title.add("What is the problem?");
        title.add("What fittings are affected?");
        title.add("Please describe the job in detail");
        title.add("When do you need it?");
        title.add("Attachments (Optional)");

        ArrayList<String> subtitle = new ArrayList<>();
        subtitle.add("");
        subtitle.add("");
        subtitle.add("Optional");
        subtitle.add("");
        subtitle.add("You may attach up to 3 sample image of the design you like");

        ArrayList<Boolean> isInput = new ArrayList<>();
        isInput.add(true);
        isInput.add(true);
        isInput.add(false);
        isInput.add(false);
        isInput.add(false);

        ArrayList<Integer> viewTypes = new ArrayList<>();
        viewTypes.add(1);
        viewTypes.add(1);
        viewTypes.add(3);
        viewTypes.add(4);
        viewTypes.add(5);

        ArrayList<ArrayList<String>> answers = new ArrayList<>();

        // 1st title
        ArrayList<String> firstTitle = new ArrayList<>();
        firstTitle.add("Leaking / burst pipe");
        firstTitle.add("Clogged drain");
        firstTitle.add("Low water / no pressure");
        firstTitle.add("Fixture not flushing");
        firstTitle.add("I'm not sure");
        ArrayList<String> secondTitle = new ArrayList<>();
        secondTitle.add("Toilet bowl");
        secondTitle.add("Sink / basin");
        secondTitle.add("Shower head");
        secondTitle.add("Shower");
        secondTitle.add("Bathtub");
        secondTitle.add("Water pump");

        answers.add(firstTitle);
        answers.add(secondTitle);
        answers.add(null);
        answers.add(null);
        answers.add(null);

        service.setServiceName("Plumbing Repair");
        service.setTitle(title);
        service.setSubtitle(subtitle);
        service.setViewTypes(viewTypes);
        service.setIsInput(isInput);
        service.setAnswers(answers);

        return service;
    }

}