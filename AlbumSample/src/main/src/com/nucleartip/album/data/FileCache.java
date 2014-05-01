/**
 * Utility class for storing downloaded images into two different cache,
 * this class is used along with a list view for better processing of image data
 */
package com.nucleartip.album.data;

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

public class FileCache {

	private static File cacheDir;
	private static final int MAX_SIZE = 100;
	private int currentCount = 0;
	private static FileCache fCache;
	private MemoryCache mChache;
	
	private FileCache(Context context){
		cacheDir = context.getCacheDir();
		File[] files = cacheDir.listFiles();
		mChache = MemoryCache.getInstance();
		if(files != null){
			currentCount = files.length;
		}else{
			currentCount = 0;
		}
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
		return fCache;
	}
	/**
	 * Returns a bitmap only if it exist in cache
	 * @param uri
	 * @return
	 */
	public Bitmap getBitmapFromCache(String uri){
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
		map = getBitmapFromCache(uri);
		if(map != null){
			return map;
		}else{
			String fileName = String.valueOf(uri.hashCode());
			File f = new File(cacheDir,fileName);
			InputStream is = null;
			FileOutputStream fos = null;
			try{
				if(currentCount > MAX_SIZE){
					if(cacheDir.listFiles() !=null){
						cacheDir.listFiles()[0].delete();
					}
					currentCount--;
				}
				URL aUrl = new URL(uri);
				URLConnection conn = aUrl.openConnection();
				conn.setUseCaches(true);
				conn.connect();
				is = conn.getInputStream();
				fos = new FileOutputStream(f);
				byte[] data = new byte[1024];
				int n = 0;
				while((n=is.read(data))!=-1){
					fos.write(data, 0, n);
				}
				currentCount++;
				Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 1;
				opts.inPurgeable = true;
				Bitmap aMap =  BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
				mChache.putBitmapIntoMemCache(uri, aMap);
				return aMap;
			}catch(Exception e){
				
			}finally{
				if(fos != null){
					try {
						fos.flush();
						fos.close();
						is.close();
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
}
