package com.nucleartip.album.ui;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.nucleartip.album.common.AlbumConstants;
import com.nucleartip.album.common.Item;
import com.nucleartip.album.common.ItemsAdapter;
import com.nucleartip.album.data.ListDataFetcher;
import com.nucleartip.album.data.ListDataFetcher.DataCallback;


public class ItemsList extends ListFragment {

	private int offset = 0;
	private Context mContext;
	private ListDataFetcher mFetcher;
	private ItemsAdapter mAdapter;
	private boolean isEndOfList = false;
	private ArrayList<Item> mItems;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mFetcher = ListDataFetcher.getInstance(mContext);
        mItems = new ArrayList<Item>();
    	mAdapter = new ItemsAdapter(mItems, getActivity());
    	setListAdapter(mAdapter);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
        setUpList();
    }
    
    @Override
    public void onResume() {
    	super.onResume();

    }

    
    /*
     * We will load a fixed numbers of items at a time, instead of loading whole of a list
     * setup will be more like infinite scroll, until we run out of items to display
     */
    private void setUpList(){
    	// Lets fetch intial set of data
        performLookup(offset);
    	getListView().setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if((firstVisibleItem+visibleItemCount) == totalItemCount && 
						totalItemCount != 0 && visibleItemCount != totalItemCount){
			       offset = totalItemCount;
			       if(!isEndOfList){
			    	   performLookup(offset);
			       }
				}
				
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
    		
    	});
    	
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	// release loaders and cache
    	mFetcher.release();
    }
    
    private void performLookup(int offset){
    	Bundle args = getArguments();
    	if(args != null
    			&& args.containsKey(AlbumConstants.KEY_DATA_TYPE)){
    		 int dataType = args.getInt(AlbumConstants.KEY_DATA_TYPE);
    		 mFetcher.getListItems(dataType,offset,new ListManager());

    	}
    	
    	
           		
    }
    
    private class ListManager implements DataCallback{

		@Override
		public void onDataAvailable(ArrayList<Item> list) {
			if(mItems == null){
				mItems = new ArrayList<Item>();
				mItems.addAll(list);
				mAdapter.notifyDataSetChanged();
			}else{
	    		mItems.addAll(list);
	    		mAdapter.notifyDataSetChanged();
			}
			
		}

		@Override
		public void onError() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLimitReached() {
			isEndOfList = true;
	    }
    	
    }
    
}
