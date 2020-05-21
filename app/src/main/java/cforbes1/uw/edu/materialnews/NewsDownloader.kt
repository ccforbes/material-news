package cforbes1.uw.edu.materialnews

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

class NewsDownloader {
    companion object {
        private var queue: RequestQueue? = null

        fun newsDataRequestQueue(c: Context): RequestQueue? {
            queue = Volley.newRequestQueue(c)
            return queue
        }

        val imageLoader: ImageLoader by lazy { //only instantiate when needed
            ImageLoader(
                queue,
                object : ImageLoader.ImageCache { //anonymous cache object
                    private val cache = LruCache<String, Bitmap>(20)
                    override fun getBitmap(url: String): Bitmap? {
                        return cache.get(url)
                    }
                    override fun putBitmap(url: String, bitmap: Bitmap) {
                        cache.put(url, bitmap)
                    }
                })
        }
    }

}