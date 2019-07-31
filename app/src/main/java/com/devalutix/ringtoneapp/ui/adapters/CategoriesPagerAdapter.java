package com.devalutix.ringtoneapp.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.pojo.Category;
import com.devalutix.ringtoneapp.ui.activities.RingtonesActivity;

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
        CardView c_container = layout.findViewById(R.id.category_container);
        ImageView c_thumbnail = layout.findViewById(R.id.category_thumbnail);
        TextView c_title = layout.findViewById(R.id.category_title);

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
        c_container.setOnClickListener(v -> {
            Intent i = new Intent(mContext, RingtonesActivity.class);
            i.putExtra("mode", "category");
            i.putExtra("name", categories.get(position).getCategoryTitle());

            mContext.startActivity(i);
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
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public float getPageWidth(int position) {
        return 0.4f;
    }
}
