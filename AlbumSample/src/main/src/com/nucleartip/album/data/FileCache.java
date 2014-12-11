/**
 * Utility class for storing downloaded images into two different cache,
 * this class is used along with a list view for better processing of image data
 */
package com.eaxmple.memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;

public class FileCache {

	private static File cacheDir;
	private static final int MAX_SIZE = 500;
	private int currentCount = 0;
	private static FileCache fCache;
	private MemoryCache mChache;
	private Context mContext;

	private FileCache(Context context){
		mContext = context;
		cacheDir = context.getCacheDir();
		File[] files = cacheDir.listFiles();
		mChache = MemoryCache.getInstance();
		if(files != null){
			currentCount = files.length;
		}else{
			currentCount = 0;
		}
	}
	private void refreshContxt(Context context){
		this.mContext = context;
	}
	/**
	 * Returns a non null instance of file cache
	 * @param context
	 * @return
	 */
	public static synchronized FileCache getInstance(Context context){
		if(fCache == null){
			fCache = new FileCache(context);
			return fCache;
		}
		fCache.refreshContxt(context);
		return fCache;
	}
	/**
	 * Returns a bitmap only if it exist in cache
	 * @param uri
	 * @return
	 */
	public Bitmap getBitmapFromCache(String uri,int width,int height){
		Bitmap map = null;
		try{
			if(mChache.getBitmapFromMemCache(uri)!=null){
				return mChache.getBitmapFromMemCache(uri);
			}
			String fileName = String.valueOf(uri.hashCode());
			File f = new File(cacheDir,fileName);
			if(f.exists()){
				Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 1;
				opts.outHeight=height;
				opts.outWidth=width;
				opts.inPurgeable = true;
				return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);

			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * Return a bitmap if present in cache, else downloads it and returns the  equivalent bitmap
	 * saving it into cache for faster processing next time
	 * @param context a non null instance of context
	 * @param uri URL of image to be doenloaded
	 * @param width 
	 * @param height
	 * @return
	 */
	public Bitmap getFile(Context context,String uri,int width,int height){
		Bitmap map = null;
		map = getBitmapFromCache(uri,width,height);
		if(map != null){
			return map;
		}else{
			String fileName = String.valueOf(uri.hashCode());
			File f = new File(cacheDir,fileName);
			InputStream is = null;
			FileOutputStream fos = null;
			Bitmap mMap = null;

			try{
				if(currentCount > MAX_SIZE){
					if(cacheDir.listFiles() !=null){
						cacheDir.listFiles()[0].delete();
					}
					currentCount--;
				}

				if(uri.startsWith(MediaStore.Images.Media.
						EXTERNAL_CONTENT_URI.toString())){
					// First lets fetch thumbail directly
					Uri temp = Uri.parse(uri);
					long id = Long.parseLong(temp.getLastPathSegment());
					mMap = MediaStore.Images.Thumbnails.
							getThumbnail(mContext.getContentResolver(),id,
									MediaStore.Images.Thumbnails.MINI_KIND,null);
					if(mMap == null){
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(uri, options);
						options.inSampleSize = calculateInSampleSize(options, width, height);
						options.inJustDecodeBounds = false;

						mMap = BitmapFactory.decodeFile(uri, options);

					}


				}else if(uri.contains("http")){
					// Internet
					URL aUrl = new URL(uri);
					URLConnection conn = aUrl.openConnection();
					conn.setUseCaches(true);
					conn.connect();
					is = conn.getInputStream();

					
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeStream(is, null, options);
					options.inSampleSize = calculateInSampleSize(options, width, height);
					options.inJustDecodeBounds = false;
					mMap = BitmapFactory.decodeStream(is, null, options);
					if(is!= null){
						is.close();
					}

				}
				if(mMap == null){
					return null;
				}

				// save this bitmap into file now
				fos = new FileOutputStream(f);
				mMap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
				currentCount++;
				mChache.putBitmapIntoMemCache(uri, mMap);

				return mMap;
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(fos != null){
					try {
						fos.flush();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}
		return map;
	}
	/**
	 * Releases all the memory acquired for cache purposes
	 */
	public void clear(){
		mChache.clear();
		File[] files = cacheDir.listFiles();
		if(files != null){
			for(File f: files){
				f.delete();
			}
		}
	}
	/**
	 * releases all the space acquired by memory cache
	 */
	public void clearMemoryCache(){
		mChache.clear();
	}


	// to calculate sample size, of large bitmaps
	public int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
