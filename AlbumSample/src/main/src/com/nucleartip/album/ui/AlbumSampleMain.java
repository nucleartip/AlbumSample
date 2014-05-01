package com.nucleartip.album.ui;




import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.Window;

import com.nucleartip.album.common.AlbumConstants;
import com.nucleartip.album.R;



public class AlbumSampleMain extends Activity
implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private DrawerLayout mDrawer;
	private boolean isDrawerOpen = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR); 
		setContentView(R.layout.activity_album_sample_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment)
				getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(
				R.id.navigation_drawer,mDrawer
				);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Lets control drawer opening and closing through home action home up as well
		if(item.getItemId() == android.R.id.home){
			if(isDrawerOpen){
				mDrawer.closeDrawer(findViewById(R.id.navigation_drawer));
				isDrawerOpen = false;
			}else{
				mDrawer.openDrawer(findViewById(R.id.navigation_drawer));
			}

		}

		return super.onOptionsItemSelected(item);
	}	

	@Override
	public void onNavigationDrawerItemSelected(int position) {


		Bundle args = new Bundle();
		args.putInt(AlbumConstants.KEY_DATA_TYPE, position);
		FragmentManager fragmentManager = getFragmentManager();
		ItemsList itemsList = new ItemsList();
		itemsList.setArguments(args);
		fragmentManager.beginTransaction()
		.replace(R.id.container, itemsList)
		.commit();
		refreshTitle(position);
	}

	private void refreshTitle(int position){
		String[] arr = getResources().getStringArray(R.array.drawer_items);
		String title = arr[position];
		if(getActionBar()!=null)
			getActionBar().setTitle(title);
	}

	@Override
	public void onDraweropen() {
		isDrawerOpen = true;
		if(getActionBar()!=null)
			getActionBar().setTitle(getString(R.string.app_name));		
	}
	@Override
	public void onDrawerclose() {
		isDrawerOpen = false;

	}

}
