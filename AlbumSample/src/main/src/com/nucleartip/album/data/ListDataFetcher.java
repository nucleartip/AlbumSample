package com.nucleartip.album.data;
/**
 * Utility class which assist in better management of data which is associated with a 
 * Scrolling view such as ListView.
 */
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.nucleartip.album.common.AlbumConstants;
import com.nucleartip.album.common.Item;

public class ListDataFetcher {

	// member variable
	private static ListDataFetcher mFetcher;
	private final int MAX_ITEMS = 1000;
	private final int MAX_FETCH = 100;
	private FileCache fCache;
	private ImageLoader[] mLoaders;
	private int curentLoader = 0;
	
	// Listener interfaces, to provide callback feature
	public interface DataCallback{
		void onDataAvailable(ArrayList<Item> list);
		void onError();
		void onLimitReached();
	}

	public interface ImageCallback
	{
		void onImageAvailable(String uri,Bitmap map);
		void onError(String uri);
	}
	private ListDataFetcher(Context context){
		this.fCache = FileCache.getInstance(context);
		// To optimize it further we can make number of loaders depending upon number of CPU present in mobile
		mLoaders = new ImageLoader[4];
		for(int i = 0; i <4 ; i++){
			mLoaders[i] = new ImageLoader("Loader:"+i);
			mLoaders[i].init();

		}
		curentLoader = 0;
	}
	/**
	 * Returns a non null single instance of data fetcher
	 * @return
	 */
	public static ListDataFetcher getInstance(Context context){
		if(mFetcher != null){
			return mFetcher;
		}else{
			mFetcher = new ListDataFetcher(context);
			return mFetcher;
		}
	}
	/**
	 * This API provides async call capability for loading data, which helps
	 * in providing smooth scroll
	 * 
	 * @param startIndex point starting from which data to be retrieved
	 * @param callback listener which will be used to notify back 
	 */
	public void getListItems(int dataType,int startIndex,
			DataCallback callback){
		new DataFetchTask(dataType,startIndex,callback).execute();
	}

	/**
	 * Async API which returns a bitmap against supplied URL,
	 * This internally employs cache, which make sure frequent 
	 * visited images are not re downloaded again and again which saves
	 * bandwidth and loading time,it also loads up images
	 * into memory cache, which makes scrolling of listview smooth
	 * 
	 * @param context
	 * @param uri URI for locating image
	 * @param widht 
	 * @param height
	 * @param callback Non null listener interface, upon success client will be notified back
	 */
	public void getImageBitmap(Context context,String uri,
			int width,int height,ImageCallback callback){

		if(uri == null || uri.equals("")){
			return;
		}
		// Prepare a request task
		ImageDownloadRequest request = new ImageDownloadRequest();
		request.context =context;
		request.width = width;
		request.height =height;
		request.uri = uri;
		request.callback = callback;
		// Check which loader is free
		boolean sent = false;
		for(int i = 0; i < 4; i++){
			if(mLoaders[i].isMessageQueueEmpty()){
				Message msg  = Message.obtain();
				msg.what = ImageLoader.DOWNLOAD_MESSAGE;
				msg.obj = request;
				mLoaders[i].sendMessage(msg);
				sent = true;
			}
		}
		if(!sent){
			// do round robin
			if(curentLoader == 3)
				curentLoader = 0;
			Message msg  = Message.obtain();
			msg.what = ImageLoader.DOWNLOAD_MESSAGE;
			msg.obj = request;

			mLoaders[curentLoader].sendMessage(msg);
			curentLoader++;
		}

	}


	private boolean isMaxItemsReached(int index){
		if(index >= MAX_ITEMS){
			return true;
		}
		return false;
	}

	public void release(){
		if(fCache!=null){
			// release only memory cache, keeping file cache intact
			fCache.clearMemoryCache();
		}
		if(mLoaders == null){
			return;
		}
		// Terminate all the loaders
		for(int i = 0; i <4 ; i++){
			mLoaders[i] = new ImageLoader("Loader:"+i);
			mLoaders[i].terminate();

		}
		mLoaders = null;
	}



	/**
	 * Data will be fetched asynchronously, this will make sure we don't hamper UI 
	 * Once data is available, client will be notified through observer callback
	 * 
	 * this method will be helpful when fetching data takes time, specially when data is 
	 * gathered via webservices
	 * @author Robin
	 *
	 */
	private class DataFetchTask extends AsyncTask<Void,Integer,ArrayList<Item>>{

		private int offset;
		private DataCallback mCallback;
		private int dataType;
		private boolean onError = false;
		public DataFetchTask(int dataType,int offset,DataCallback callback){
			this.dataType = dataType;
			this.offset = offset;
			this.mCallback = callback;
		}

		@Override
		protected ArrayList<Item> doInBackground(Void... params) {

			if(isMaxItemsReached(offset)){
				mCallback.onLimitReached();
				return null;
			}
			ArrayList<Item> items = null;
			try{
				items = new ArrayList<Item>();

				if(dataType == AlbumConstants.ITEM_TYPE_ALBUM){
					for(int i = offset ; i < offset+MAX_FETCH ; i++){
						Item item = new Item();
						item.setMainHeader("Album " + i);
						item.setSecondaryHeader("Artist " + i);
						items.add(item);
					}					
				}
				if(dataType == AlbumConstants.ITEM_TYPE_SONGS){
					for(int i = offset ; i < offset+MAX_FETCH ; i++){
						Item item = new Item();
						item.setMainHeader("Song " + i);
						item.setSecondaryHeader("Genre " + i);
						items.add(item);
					}					
				}
				if(dataType == AlbumConstants.ITEM_TYPE_ARTIST){
					for(int i = offset ; i < offset+MAX_FETCH ; i++){
						Item item = new Item();
						item.setMainHeader("Artist " + i);
						item.setSecondaryHeader("Gender " + i);
						items.add(item);
					}					
				}

			}catch(Exception e){
				e.printStackTrace();
				onError = true;
			}

			return items;

		}

		@Override
		protected void onPostExecute(ArrayList<Item> result) {
			if(result != null){
				mCallback.onDataAvailable(result);
			}else if(onError){
				mCallback.onError();
			}
		}

	}

    /**
     * Support calss to hold image downloading data until its processed
     * @author Robin
     *
     */
	private class ImageDownloadRequest{
		public String uri;
		public int width;
		public int height;
		public ImageCallback callback;
		public Context context;
	}

	/**
	 * A fixed number of threaded pool, this pool helps in downloading and processing multiple
	 * images at once, Idea behind using fixed pool rather then AsyncTask is to avoid recent
	 * changes which executes all the asyn task one after another rather than parallely, unless
	 * they are executed using a executor service, which creates additional overhead.
	 * 
	 * @author Robin
	 *
	 */
	private class ImageLoader extends Thread{

		private Handler mHandler;
		private Looper myLooper;
		public static final int DOWNLOAD_MESSAGE = 0;
		public ImageLoader(String name){
			super(name);
		}

		public void init(){
			start();
		}

		public void terminate(){
			if(myLooper!=null){
				myLooper.quit();
			}
		}
        /**
         * Sends a message to thread for processing
         * @param msg
         */
		public void sendMessage(Message msg){
			if(isAlive()){
				mHandler.sendMessage(msg);
			}else{
				init();
				mHandler.sendMessage(msg);
			}

		}
        /**
         * Checks if current thread message queue is empty
         * @return
         */
		public boolean isMessageQueueEmpty(){
			if(mHandler!=null){
				return mHandler.hasMessages(DOWNLOAD_MESSAGE);
			}else{
				init();
				return true;
			}
		}
		@SuppressLint("HandlerLeak")
		@Override
		public void run() {
			super.run();
			Looper.prepare();
			myLooper = Looper.myLooper();
			mHandler = new Handler(){
				public void handleMessage(android.os.Message msg) {
					ImageDownloadRequest request = (ImageDownloadRequest)msg.obj;
					if(request != null){
						if(fCache != null){
							Bitmap map = fCache.getFile(request.context, request.uri,
									request.width, request.height);
							if(map != null){
								if(request.callback != null){
									request.callback.onImageAvailable(request.uri, map);
								}
							}else{
								if(request.callback != null){
									request.callback.onError(request.uri);
								}
							}
						}
					}
				};
			};
			Looper.loop();
		}
	}

}
