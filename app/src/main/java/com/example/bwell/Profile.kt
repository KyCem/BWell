// com/example/bwell/Profile.kt
package com.example.bwell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bwell.model.LeaderBoardItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class Profile : Fragment() {

    private lateinit var tvName: TextView      // R.id.tvName
    private lateinit var tvScoreBig: TextView  // R.id.tvRight
    private lateinit var rvLeaderboard: RecyclerView
    private val adapter = LeaderboardAdapter()

    private lateinit var db: FirebaseFirestore
    private var userSub: ListenerRegistration? = null
    private var listSub: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tvName)
        tvScoreBig = view.findViewById(R.id.tvRight)
        rvLeaderboard = view.findViewById(R.id.rvLeaderboard)

        rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        rvLeaderboard.adapter = adapter
        rvLeaderboard.isNestedScrollingEnabled = false

        db = FirebaseFirestore.getInstance()

        subscribeUserHeader()
        subscribeLeaderboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userSub?.remove(); userSub = null
        listSub?.remove(); listSub = null
    }

    /** Dummy user listener (replace with FirebaseAuth later) */
    private fun subscribeUserHeader() {
        userSub?.remove()

        // --- FirebaseAuth would normally go here ---
        // val uid = FirebaseAuth.getInstance().currentUser?.uid
        // if (uid == null) { ... }

        // For now, just use a fixed document id:
        val dummyUserId = "demoUser123"

        userSub = db.collection("leaderboard")
            .document(dummyUserId)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null || !snap.exists()) {
                    tvName.text = "Unknown"
                    tvScoreBig.text = "0"
                    return@addSnapshotListener
                }

                val name = snap.getString("name") ?: "Unknown"
                val score = snap.getLong("score") ?: 0L
                tvName.text = name
                tvScoreBig.text = score.toString()
            }
    }

    /** Leaderboard list */
    private fun subscribeLeaderboard(limit: Long = 50) {
        listSub?.remove()

        listSub = db.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { qs, err ->
                if (err != null || qs == null) {
                    adapter.replaceAll(emptyList())
                    return@addSnapshotListener
                }
                val items = qs.documents.map { d ->
                    LeaderBoardItem(
                        uid   = d.id,
                        name  = d.getString("name") ?: "Unknown",
                        score = d.getLong("score") ?: 0L
                    )
                }
                adapter.replaceAll(items)
            }
    }
}
