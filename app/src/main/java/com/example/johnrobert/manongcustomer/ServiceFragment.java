package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ServiceFragment extends Fragment {

    public RecyclerView recyclerView;
    private static final int REQUEST_ID_DETAIL = 2;
    private ImplementationAdapter adapter;
    private Activity activity;

    public ServiceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_service, container, false);

        activity = getActivity();
        setupRecyclerView(view);

        return view;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    private void setupRecyclerView(View view) {

        recyclerView = view.findViewById(R.id.implementation_list);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new ImplementationAdapter(getContext(), (fromView, item) -> {
            final Intent intent = new Intent(getContext(), item.getActivityClass());
            intent.putExtra(ServiceDetailActivity.INTENT_EXTRA_ITEM, item);
            String sharedElementName = getString(R.string.transition_name_implementation_image);
            final Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, fromView, sharedElementName).toBundle();
            ActivityCompat.startActivityForResult(activity, intent, REQUEST_ID_DETAIL, options);
        });
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int itemViewType = adapter.getItemViewType(position);
                return itemViewType == ImplementationAdapter.VIEW_TYPE_HEADER ? 2 : 1;
            }
        });

        recyclerView.setAdapter(adapter);

        // Calc grid space
        final float spaceSize = ScreenUtil.dp2px(16, activity);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int adapterPosition = parent.getChildViewHolder(view).getAdapterPosition();
                GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
                int spanSize = spanSizeLookup.getSpanSize(adapterPosition);
                if (spanSize == 2) {
                    return;
                }
                int spanIndex = spanSizeLookup.getSpanIndex(adapterPosition, gridLayoutManager.getSpanCount());
                if (spanIndex == 0) {
                    outRect.set((int) spaceSize, (int) spaceSize, ((int) (spaceSize / 2)), 0);
                } else {
                    outRect.set(((int) (spaceSize / 2)), (int) spaceSize, (int) spaceSize, 0);
                }
            }
        });

        ((ScrollView) view.findViewById(R.id.content_main)).fullScroll(ScrollView.FOCUS_UP);
        ((ScrollView) view.findViewById(R.id.content_main)).smoothScrollTo(0,0);
    }

}
