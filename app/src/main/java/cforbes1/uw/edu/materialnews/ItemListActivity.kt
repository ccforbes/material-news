package cforbes1.uw.edu.materialnews

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.*
import android.widget.ActionMenuView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import cforbes1.uw.edu.materialnews.NewsDownloader.Companion.imageLoader
import cforbes1.uw.edu.materialnews.NewsDownloader.Companion.newsDataRequestQueue
//import cforbes1.uw.edu.materialnews.NewsDownloader.Companion.requestQueue

import cforbes1.uw.edu.materialnews.dummy.DummyContent
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import kotlinx.android.synthetic.main.item_list.*

import com.android.volley.toolbox.NetworkImageView

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false
    private var newsArticles = mutableListOf<NewsArticle>()
    private var query: String = ""
    private val QUERY_STRING: String = "search query"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        refreshFabOnClick()
        showRefreshHideShare()


        if (item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
            val fragment = Welcome.newInstance("Powered by NewsAPI.org")
            supportFragmentManager.beginTransaction().run {
                replace(R.id.item_detail_container, fragment)
                addToBackStack(null)
                commit()
            }
        }

        populateNewsArticles(newsUriBuilder())


        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY_STRING)!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val menuSearchView = menu.findItem(R.id.menu_search)?.actionView as SearchView
        menuSearchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)
        }

        val expandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                return true // Return true to collapse action view
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true // Return true to expand action view
            }
        }

        val actionMenuItem = menu.findItem(R.id.menu_search)
        actionMenuItem?.setOnActionExpandListener(expandListener)

        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.putString(QUERY_STRING, query)
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        fetchBasedOnIntent(intent)
    }

    // sends search intent based on query
    private fun fetchBasedOnIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            query = intent.getStringExtra(SearchManager.QUERY)
        } else {
            query = ""
        }
        populateNewsArticles(newsUriBuilder())
    }

    // parses JsonObjectRequest to get a list of NewsArticles
    private fun populateNewsArticles(url: String) {
        val queue = newsDataRequestQueue(applicationContext)

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener { response ->
                newsArticles = parseNewsAPI(response) as MutableList<NewsArticle>
                setupRecyclerView(item_list, newsArticles)
                (item_list.adapter as SimpleItemRecyclerViewAdapter).notifyDataSetChanged()
            },
            Response.ErrorListener { error ->

            })
        queue?.add(request)
    }

    // Sets up the RecyclerView's adapter and layoutManager
    private fun setupRecyclerView(recyclerView: RecyclerView, newsArticles: MutableList<NewsArticle>) {
        if (twoPane) {
            recyclerView.layoutManager = GridLayoutManager(this, 1)
        } else {
            recyclerView.layoutManager = GridLayoutManager(this, 2)
        }

        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, newsArticles, twoPane)
    }

    // Builds the URI for the JsonObjectRequest
    private fun newsUriBuilder(): String {
        val builder = Uri.Builder()
        builder.scheme("https").authority("newsapi.org")
        if (query == "") {
            builder.appendPath("v2").appendPath("top-headlines")
            builder.appendQueryParameter("country", "us").appendQueryParameter("language", "en")
        } else {
            builder.appendPath("v2").appendPath("everything")
            builder.appendQueryParameter("q", query)
        }
        builder.appendQueryParameter("apiKey", getString(R.string.OPEN_NEWS_API_KEY))
        return builder.toString()
    }

    // sets up on click listener for share FAB
    private fun shareFabOnClick(article: NewsArticle) {
        share_fab.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Breaking news! " + article.webUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    // sets up on click listener for refresh FAB
    private fun refreshFabOnClick() {
        fab.setOnClickListener {
            populateNewsArticles(newsUriBuilder())
        }
    }

    // hides refresh button, show's share button
    private fun hideRefreshShowShare() {
        fab.hide()
        share_fab.show()
    }

    // shows refresh button, hides share button
    private fun showRefreshHideShare() {
        fab.show()
        share_fab.hide()
    }


    inner class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity,
        private val values: List<NewsArticle>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as NewsArticle
                println(item.headline + " : " +  item.description)
                if (twoPane) {
                    val fragment = ItemDetailFragment.newInstance(item)
                    parentActivity.supportFragmentManager.beginTransaction().run {
                        replace(R.id.item_detail_container, fragment)
                        addToBackStack(null)
                        commit()
                    }
                    shareFabOnClick(item)
                    hideRefreshShowShare()

                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.headline.text = item.headline
            holder.image.setDefaultImageResId(R.drawable.newspaper)
            holder.image.setImageUrl(item.imageUrl, imageLoader)

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val headline: TextView = view.news_headline
            val image: NetworkImageView = view.news_image
        }
    }
}
