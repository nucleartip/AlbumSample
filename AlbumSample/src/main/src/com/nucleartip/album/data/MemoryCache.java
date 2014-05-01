package com.nucleartip.album.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.graphics.Bitmap;


public class MemoryCache {
    private Map<String, Bitmap> cache=Collections.synchronizedMap(
            new LinkedHashMap<String, Bitmap>(10,1.5f,true));
    private long size=0;
    private long limit;
    private static MemoryCache mChache;
    
    private MemoryCache(){
    	limit = Runtime.getRuntime().maxMemory()/4;
    }
    
    public static synchronized MemoryCache getInstance(){
    	if(mChache != null){
    		return mChache;
    	}else{
    		mChache = new MemoryCache();
    		return mChache;
    	}
    }
    
    /**
     * Return a bitmap from memory
     * @param id to uniquely locate a image, e,g URI
     * @return
     */
    public Bitmap getBitmapFromMemCache(String id){
        if(!cache.containsKey(id))
            return null;
        return cache.get(id);
    }

    /**
     * Puts a bitmap into memory cache
     * @param id key
     * @param bitmap image
     */
    public void putBitmapIntoMemCache(String id, Bitmap bitmap){
        try{
            if(cache.containsKey(id))
                size-=getSizeInBytes(cache.get(id));
            cache.put(id, bitmap);
            size+=getSizeInBytes(bitmap);
            checkSize();
        }catch(Throwable th){
            th.printStackTrace();
        }
    }
    /**
     * Util function to check size, and if limit breaches, it will remove old bitmaps 
     * until new one is accommodated
     */
    private void checkSize() {
        if(size>limit){
            Iterator<Entry<String, Bitmap>> iter=cache.entrySet().iterator();
            while(iter.hasNext()){
                Entry<String, Bitmap> entry=iter.next();
                size-=getSizeInBytes(entry.getValue());
                iter.remove();
                if(size<=limit)
                    break;
            }
        }
    }

    public void clear() {
        cache.clear();
    }

    long getSizeInBytes(Bitmap bitmap) {
        if(bitmap==null)
            return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
