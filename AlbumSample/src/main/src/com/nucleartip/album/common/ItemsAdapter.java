package com.nucleartip.album.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.nucleartip.album.data.ListDataFetcher;
import com.nucleartip.album.data.ListDataFetcher.ImageCallback;
import com.nucleartip.album.R;


public class ItemsAdapter extends BaseAdapter {
    private ArrayList<Item> items;
    private Context mContext;
    private ListDataFetcher mFetcher;
    
    public ItemsAdapter(ArrayList<Item> items, Context mContext) {
        this.items = items;
        this.mContext = mContext;
        mFetcher = ListDataFetcher.getInstance(mContext);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // iF view already exist lets reuse it
    	if(view == null){
            LayoutInflater inflater = (LayoutInflater) mContext.
            		getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item, viewGroup, false);
    	}
    	

        final Item item = (Item) getItem(i);
        ((TextView) view.findViewById(R.id.main_header)).setText(item.getMainHeader());
        ((TextView) view.findViewById(R.id.secondary_header)).setText(item.getSecondaryHeader());
        ImageView mImage = (ImageView)view.findViewById(R.id.icon);
        
        mImage.setImageResource(R.drawable.placeholder);
        // at this point we can return the view, by simply loading a default image into view
        // this will make sure smooth scroll
        
        // Now we submit a image loader task to system, upon receiving which we will replace
        // default image to actual one.
        mFetcher.getImageBitmap(mContext, item.getImageUri(),
        		128, 128, new ImageCallback(){

					@Override
					public void onImageAvailable(String uri, Bitmap map) {
						if(uri != null && item.getImageUri() != null){
							if(uri.equals(item.getImageUri())){
								// Set the image here
							}
						}

						
					}

					@Override
					public void onError(String uri) {
						// do something here
						
					}
        	
        });
        
        
        return view;
    }
}
