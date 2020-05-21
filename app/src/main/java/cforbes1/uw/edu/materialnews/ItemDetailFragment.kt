package cforbes1.uw.edu.materialnews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cforbes1.uw.edu.materialnews.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.description

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */

class ItemDetailFragment : Fragment() {

    interface HasCollapsibleToolbar {
        fun setupToolbar()
    }

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: NewsArticle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = it.getParcelable(ARG_ITEM_ID)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.item_detail, container, false)
        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.headline.text = it.headline
            if (it.description != "null") {
                rootView.description.text = it.description
            } else {
                rootView.description.text = "No description available."
            }
            rootView.source.text = it.sourceName
        }

        if (activity is HasCollapsibleToolbar) {
            (activity as HasCollapsibleToolbar).setupToolbar()
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"

        fun newInstance(argument: NewsArticle) =
            ItemDetailFragment().apply{
                arguments = Bundle().apply {
                    putParcelable(ARG_ITEM_ID, argument)
                }
            }

    }
}
