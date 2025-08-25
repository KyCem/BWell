package com.example.bwell.ui.news

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bwell.R
import com.example.bwell.databinding.FragmentHomeBinding
import com.example.bwell.databinding.ItemNewsBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "NewsFragment"

data class NewsItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val createdAt: Timestamp?,
    val views: Long
)

class NewsFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }

    private val newsAdapter = NewsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNews.adapter = newsAdapter

        loadNewsFromFirestore()
    }

    private fun loadNewsFromFirestore() {

        db.collection("news")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { snap ->
                Log.d(TAG, "Firestore success. docs=${snap.size()}")
                val items = snap.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    NewsItem(
                        id = doc.id,
                        title = title,
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        createdAt = doc.getTimestamp("createdAt"),
                        views = doc.getLong("views") ?: 0L
                    )
                }

                newsAdapter.submitList(items) {
                    val count = newsAdapter.itemCount
                    if (count == 0) {
                        binding.progressLoading.visibility = View.VISIBLE
                        //binding.tvStatus.text = "No news found."
                    } else {
                        binding.progressLoading.visibility = View.GONE
                    }
                    Log.d(TAG, "Adapter now has $count items")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore load failed", e)
                binding.progressLoading.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Load failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

private class NewsAdapter :
    ListAdapter<NewsItem, NewsAdapter.NewsVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsVH {
        val binding = ItemNewsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NewsVH(binding)
    }

    override fun onBindViewHolder(holder: NewsVH, position: Int) {
        holder.bind(getItem(position))
    }

    class NewsVH(
        private val binding: ItemNewsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(item: NewsItem) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description

            // If you added a meta TextView (date + views) to your item layout, set it here:
            // val dateStr = item.createdAt?.toDate()?.let { dateFmt.format(it) }
            // binding.tvMeta.text = listOfNotNull(dateStr, "${item.views} views").joinToString(" · ")

            binding.ivCover.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.baseline_event_busy_24) // replace with your placeholder
                error(R.drawable.baseline_event_busy_24)       // replace with your error drawable
            }

            binding.cardRoot.setOnClickListener {
                // Read-only: no updates. If you want to open a detail screen, do it here.
                // Example: Toast.makeText(binding.root.context, item.title, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<NewsItem>() {
            override fun areItemsTheSame(old: NewsItem, new: NewsItem) = old.id == new.id
            override fun areContentsTheSame(old: NewsItem, new: NewsItem) = old == new
        }
    }
}
