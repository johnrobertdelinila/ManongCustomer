package com.example.johnrobert.manongcustomer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
                        1,
                        "Plumbing",
                        R.drawable.plumbing_filipino,
                        ServiceDetailActivity.class,
                        initServicePlumber(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        2,
                        "Laundry Services",
                        R.drawable.laba_filipina,
                        ServiceDetailActivity.class,
                        initLaundryService(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        3,
                        "Appliance Repair",
                        R.drawable.reapairing_filipino,
                        ServiceDetailActivity.class,
                        initApplianceRepair(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        4,
                        "Electrical Wiring",
                        R.drawable.filipino_electrician,
                        ServiceDetailActivity.class,
                        initServiceElectric(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        5,
                        "Household Cleaning",
                        R.drawable.cleaning_filipina,
                        ServiceDetailActivity.class,
                        initHomeCleaning(new Service())
                )
        );
        listItems.add(
                new ImplementationItem(
                        6,
                        "Painting",
                        R.drawable.pinoy_taga_pintor,
                        ServiceDetailActivity.class,
                        initPainting(new Service())
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
            try {
                imageView.setImageResource(item.imageRes);
            }catch (Exception e) {
                Log.e("IMAGE EXCEPTION", e.getMessage());
            }
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

    private Service initHomeCleaning(Service service) {
        ArrayList<String> title = new ArrayList<>();
        title.add("Household");
        title.add("Upholstery");
        title.add("Stain");
        title.add("Details (optional)");
        title.add("Date");

        ArrayList<String> subTitle = new ArrayList<>();
        subTitle.add("What do you want to clean");
        subTitle.add("What type of upholstery cleaning services do you need");
        subTitle.add("Are there any stains on the upholstery that need cleaning");
        subTitle.add("Describe the cleaning");
        subTitle.add("When do you need it");

        ArrayList<ArrayList<String>> answers = new ArrayList<>();
        ArrayList<String> firstTitle = new ArrayList<>();
        firstTitle.add("Sofa");
        firstTitle.add("Curtain");
        firstTitle.add("Mattress");
        firstTitle.add("Cushion Covers");
        firstTitle.add("Chairs / Seats");
        ArrayList<String> secondTitle = new ArrayList<>();
        secondTitle.add("Shampoo");
        secondTitle.add("Dry Cleaning");
        secondTitle.add("Sanitizer / Deodorizer");
        secondTitle.add("I want a recommendation");
        ArrayList<String> thirdTitle = new ArrayList<>();
        thirdTitle.add("No stains");
        thirdTitle.add("With stains");
        answers.add(firstTitle);
        answers.add(secondTitle);
        answers.add(thirdTitle);
        answers.add(null);
        answers.add(null);

        ArrayList<Boolean> isInput = new ArrayList<>();
        isInput.add(true);
        isInput.add(false);
        isInput.add(true);
        isInput.add(false);
        isInput.add(false);

        ArrayList<Boolean> isOptional = new ArrayList<>();
        isOptional.add(false);
        isOptional.add(false);
        isOptional.add(false);
        isOptional.add(true);
        isOptional.add(false);

        ArrayList<Integer> viewTypes = new ArrayList<>();
        viewTypes.add(1);
        viewTypes.add(1);
        viewTypes.add(1);
        viewTypes.add(3);
        viewTypes.add(4);

        service.setServiceName("Household Cleaning");
        service.setTitle(title);
        service.setSubtitle(subTitle);
        service.setViewTypes(viewTypes);
        service.setIsInput(isInput);
        service.setIsOptional(isOptional);
        service.setAnswers(answers);

        return service;

    }

    private Service initPainting(Service service) {
        ArrayList<String> title = new ArrayList<>();
        title.add("Painting");
        title.add("Property");
        title.add("Details (optional)");
        title.add("Attachments (optional)");
        title.add("Date");

        ArrayList<String> subTitle = new ArrayList<>();
        subTitle.add("What needs painting");
        subTitle.add("What type of property");
        subTitle.add("Please describe the job in detail");
        subTitle.add("Attachments");
        subTitle.add("When do you need it");

        ArrayList<ArrayList<String>> answers = new ArrayList<>();
        ArrayList<String> firstTitle = new ArrayList<>();
        firstTitle.add("Interior walls / Interior doors / Ceiling");
        firstTitle.add("Exterior walls / Exteriors doors");
        firstTitle.add("Woodword");
        firstTitle.add("Metalwork");
        firstTitle.add("Windows");
        firstTitle.add("Roof");
        ArrayList<String> secondTitle = new ArrayList<>();
        secondTitle.add("Bungalow / Townhouse");
        secondTitle.add("Apartment / Condominium / Two-storey / Multilevel");
        answers.add(firstTitle);
        answers.add(secondTitle);
        answers.add(null);
        answers.add(null);
        answers.add(null);

        ArrayList<Integer> viewTypes = new ArrayList<>();
        viewTypes.add(1);
        viewTypes.add(2);
        viewTypes.add(3);
        viewTypes.add(5);
        viewTypes.add(4);

        ArrayList<Boolean> isOptional = new ArrayList<>();
        isOptional.add(false);
        isOptional.add(false);
        isOptional.add(true);
        isOptional.add(true);
        isOptional.add(false);

        ArrayList<Boolean> isInput = new ArrayList<>();
        isInput.add(false);
        isInput.add(false);
        isInput.add(false);
        isInput.add(false);
        isInput.add(false);

        service.setServiceName("Painting");
        service.setTitle(title);
        service.setSubtitle(subTitle);
        service.setViewTypes(viewTypes);
        service.setIsInput(isInput);
        service.setIsOptional(isOptional);
        service.setAnswers(answers);

        return service;

    }

    private Service initApplianceRepair(Service service) {

        ArrayList<String> title = new ArrayList<>();
        title.add("Washing Machine Dryer");
        title.add("Refrigerator");
        title.add("Aircon");
        title.add("Details (optional)");
        title.add("Date");

        ArrayList<String> subTitle = new ArrayList<>();
        subTitle.add("What's the problem in Washing Machine");
        subTitle.add("What's wrong with the refrigerator");
        subTitle.add("What's wrong with the air conditioner");
        subTitle.add("Describe the repairing work");
        subTitle.add("When do you need it");

        ArrayList<ArrayList<String>> answers = new ArrayList<>();
        ArrayList<String> firstTitle = new ArrayList<>();
        firstTitle.add("Spinner does not turn / not functioning");
        firstTitle.add("Water leaking");
        firstTitle.add("Water not draining");
        firstTitle.add("Water not draining");
        ArrayList<String> secondTitle = new ArrayList<>();
        secondTitle.add("Refrigerator not cold / not working");
        secondTitle.add("Freezer not working");
        secondTitle.add("Noisy");
        secondTitle.add("Water leaking");
        secondTitle.add("Ice maker not working");
        ArrayList<String> thirdTitle = new ArrayList<>();
        thirdTitle.add("Not cold / not working optimally");
        thirdTitle.add("Air not blowing / not blowing optimally");
        thirdTitle.add("Leaking");
        thirdTitle.add("Control / sensor problem");
        answers.add(firstTitle);
        answers.add(secondTitle);
        answers.add(thirdTitle);
        answers.add(null);
        answers.add(null);

        ArrayList<Boolean> isOptional = new ArrayList<>();
        isOptional.add(true);
        isOptional.add(true);
        isOptional.add(true);
        isOptional.add(true);
        isOptional.add(false);

        ArrayList<Boolean> isInput = new ArrayList<>();
        isInput.add(true);
        isInput.add(true);
        isInput.add(true);
        isInput.add(false);
        isInput.add(false);

        ArrayList<Integer> viewTypes = new ArrayList<>();
        viewTypes.add(1);
        viewTypes.add(1);
        viewTypes.add(1);
        viewTypes.add(3);
        viewTypes.add(4);

        service.setServiceName("Appliance Repair");
        service.setTitle(title);
        service.setSubtitle(subTitle);
        service.setViewTypes(viewTypes);
        service.setIsInput(isInput);
        service.setIsOptional(isOptional);
        service.setAnswers(answers);

        return service;

    }

    private Service initLaundryService(Service service) {
        ArrayList<String> title = new ArrayList<>();
        title.add("Clothing");
        title.add("Household");
        title.add("Date");

        ArrayList<String> subTitle = new ArrayList<>();
        subTitle.add("What are the clothing items do you need to be wash");
        subTitle.add("What are the household items that needed to be clean");
        subTitle.add("When do you need it");

        ArrayList<Boolean> isOptional = new ArrayList<>();
        isOptional.add(false);
        isOptional.add(false);
        isOptional.add(false);

        ArrayList<Boolean> isInput = new ArrayList<>();
        isInput.add(true);
        isInput.add(true);
        isInput.add(false);

        ArrayList<Integer> viewTypes = new ArrayList<>();
        viewTypes.add(1);
        viewTypes.add(1);
        viewTypes.add(4);

        ArrayList<ArrayList<String>> answers = new ArrayList<>();

        ArrayList<String> firstTitle = new ArrayList<>();
        firstTitle.add("Cap / Hat / Socks");
        firstTitle.add("T-Shirt / Tie");
        firstTitle.add("Blouse / Shirt / Trousers / Skirt / Waistcoat");
        firstTitle.add("Cardigan / Sweater / Dress / Sequin Skirt / Sequin Blouse");
        firstTitle.add("Jacket / Baju Meleyu / Baju Kabaya");
        firstTitle.add("Suit / Sequin Dress / Overcoat");

        ArrayList<String> secondTitle = new ArrayList<>();
        secondTitle.add("Bolster Case / Pillow Case");
        secondTitle.add("Cushion Cover");
        secondTitle.add("Bed Sheet");
        secondTitle.add("Blanket / Red Spread / Quit Cover / Duver Cover / Pillow");
        secondTitle.add("Comforter");
        secondTitle.add("Table Cloth");
        secondTitle.add("Curtain");

        answers.add(firstTitle);
        answers.add(secondTitle);
        answers.add(null);

        service.setServiceName("Laundry Service");
        service.setTitle(title);
        service.setSubtitle(subTitle);
        service.setViewTypes(viewTypes);
        service.setIsOptional(isOptional);
        service.setIsInput(isInput);
        service.setAnswers(answers);

        return service;
    }

    private Service initServiceElectric(Service service) {
        ArrayList<String> title = new ArrayList<>();
        title.add("Wiring");
        title.add("Type");
        title.add("Details (optional)");
        title.add("Date");

        ArrayList<String> subTitle = new ArrayList<>();
        subTitle.add("What wiring work do you need");
        subTitle.add("What's the wiring");
        subTitle.add("Describe the electrical work");
        subTitle.add("When do you need it");

        ArrayList<Boolean> isOptional = new ArrayList<>();
        isOptional.add(false);
        isOptional.add(false);
        isOptional.add(true);
        isOptional.add(false);

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
        service.setIsOptional(isOptional);
        service.setAnswers(answers);

        return service;
    }

    private Service initServicePlumber(Service service) {

        ArrayList<String> title = new ArrayList<>();
        title.add("Problem");
        title.add("Fittings");
        title.add("Details (optional)");
        title.add("Date");
        title.add("Attachments (optional)");

        ArrayList<String> subtitle = new ArrayList<>();
        subtitle.add("What is the problem");
        subtitle.add("What fittings are affected");
        subtitle.add("Describe the job in detail");
        subtitle.add("When do you need it");
        subtitle.add("You may attach up to 3 sample image of the design you like");

        ArrayList<Boolean> isOptional = new ArrayList<>();
        isOptional.add(false);
        isOptional.add(false);
        isOptional.add(true);
        isOptional.add(false);
        isOptional.add(true);

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
        service.setIsOptional(isOptional);
        service.setAnswers(answers);

        return service;
    }

}