package com.devalutix.ringtoneapp.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.pojo.Category;

import java.util.ArrayList;

public class CategoriesPagerAdapter extends PagerAdapter {

    //Declarations
    private Context mContext;
    private ArrayList<Category> categories;

    public CategoriesPagerAdapter(Context mContext, ArrayList<Category> categories) {
        this.mContext = mContext;
        this.categories = categories;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.category_item, container, false);

        //Setup and Events
        CardView c_container = (CardView) layout.findViewById(R.id.category_container);
        ImageView c_thumbnail = (ImageView) layout.findViewById(R.id.category_thumbnail);
        TextView c_title = (TextView) layout.findViewById(R.id.category_title);

        //Setup Item
        Log.d("CategoriesAdapter", "instantiateItem: title" + categories.get(position).getCategoryTitle());
        c_title.setText(categories.get(position).getCategoryTitle());
        Glide.with(mContext)
                .load(categories.get(position).getCategoryThumbnailUrl())
                .fitCenter()
                //.placeholder(R.drawable.loading_spinner)
                .into(c_thumbnail);
        c_container.setCardBackgroundColor(Color.parseColor(categories.get(position).getCategoryCardColor()));

        //Event
        c_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Go To RingtonesActivity
            }
        });

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        if (categories == null) return 0;
        else return categories.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public float getPageWidth(int position) {
        return 0.4f;
    }
}
