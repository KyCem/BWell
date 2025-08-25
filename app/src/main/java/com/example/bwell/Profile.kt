package com.example.bwell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class Profile : Fragment() {

    // If you don't have a shared model, uncomment:
    // data class LeaderboardItem(val name: String, val score: Long)

    private lateinit var tvName: TextView       // R.id.tvName (from your XML)
    private lateinit var tvScoreBig: TextView   // R.id.tvRight (the big number on the right)
    private lateinit var rvLeaderboard: RecyclerView

    private lateinit var db: FirebaseFirestore
    private var sub: ListenerRegistration? = null

    // TODO: set to your document ID in "leaderboard"
    private val userDocId: String = "YOUR_LEADERBOARD_DOC_ID"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // IDs taken from the XML you posted
        tvName = view.findViewById(R.id.tvName)
        tvScoreBig = view.findViewById(R.id.tvRight)
        rvLeaderboard = view.findViewById(R.id.rvLeaderboard)

        // Optional: set up leaderboard list if you want to show top players, otherwise you can remove this block
        if (rvLeaderboard != null) {
            rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
            // Attach your adapter instance here if you have it:
            // rvLeaderboard.adapter = LeaderboardAdapter()
            rvLeaderboard.isNestedScrollingEnabled = false
        }

        db = FirebaseFirestore.getInstance()
        startSubscription()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sub?.remove()
        sub = null
    }

    /** Realtime subscription to your user's leaderboard doc */
    private fun startSubscription() {
        sub?.remove()
        sub = db.collection("leaderboard")
            .document(userDocId)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null || !snap.exists()) {
                    tvName.text = "Unknown"
                    tvScoreBig.text = "0"
                    return@addSnapshotListener
                }
                applySnapshot(snap)
            }
    }

    private fun applySnapshot(doc: DocumentSnapshot) {
        val name = doc.getString("name") ?: "Unknown"
        val score = doc.getLong("score") ?: 0L
        tvName.text = name
        tvScoreBig.text = score.toString()
    }
}
class LeaderboardAdapter(
    private val items: MutableList<LeaderboardItem> = mutableListOf()
) : RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvItemName)
        val score: TextView = v.findViewById(R.id.tvItemScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.score.text = item.score.toString()
    }

    override fun getItemCount() = items.size

    fun replaceAll(newItems: List<Profile.LeaderboardItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
