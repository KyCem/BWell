package com.example.bwell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bwell.model.DietEntry
import com.example.bwell.ui.DietAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import android.util.Log
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.view.View

class Diet : Fragment() {

    private lateinit var rvMorning: RecyclerView
    private lateinit var rvAfternoon: RecyclerView
    private lateinit var rvEvening: RecyclerView
    private lateinit var adapterMorning: DietAdapter
    private lateinit var adapterAfternoon: DietAdapter
    private lateinit var adapterEvening: DietAdapter
    private lateinit var tvTotal: TextView
    private lateinit var tvQuality: TextView

    private val morning = mutableListOf<DietEntry>()
    private val afternoon = mutableListOf<DietEntry>()
    private val evening = mutableListOf<DietEntry>()

    // Firestore
    private lateinit var db: FirebaseFirestore
    private var dietListener: ListenerRegistration? = null

    // Add screen result (let Firestore listener update UI; we just scroll)
    private val addDietLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            // Optional: nudge UX; Firestore snapshot will refresh items
            rvMorning.smoothScrollToPosition(0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.fragment_diet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rvMorning = view.findViewById(R.id.rvMorning)
        rvAfternoon = view.findViewById(R.id.rvAfternoon)
        rvEvening = view.findViewById(R.id.rvEvening)
        tvTotal = view.findViewById(R.id.tvTotalCalories)
        tvQuality = view.findViewById(R.id.tvQualityScore)
        progressOverlay = view.findViewById(R.id.progressOverlay)

        adapterMorning = DietAdapter(morning)
        adapterAfternoon = DietAdapter(afternoon)
        adapterEvening = DietAdapter(evening)

        rvMorning.layoutManager = LinearLayoutManager(requireContext())
        rvAfternoon.layoutManager = LinearLayoutManager(requireContext())
        rvEvening.layoutManager = LinearLayoutManager(requireContext())

        rvMorning.adapter = adapterMorning
        rvAfternoon.adapter = adapterAfternoon
        rvEvening.adapter = adapterEvening

        view.findViewById<FloatingActionButton>(R.id.fabAddDiet)?.setOnClickListener {
            addDietLauncher.launch(Intent(requireContext(), AddDietItemActivity::class.java))
        }
        logFirebaseEnv()

        setLoading(true)          // <— show spinner before we subscribe
        subscribeToDiet()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        dietListener?.remove()
        dietListener = null
    }
    private lateinit var progressOverlay: View

    private fun setLoading(loading: Boolean) {
        progressOverlay.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun logFirebaseEnv() {
        val app = FirebaseApp.getInstance()
        Log.d("NewsFragment", "Firebase: projectId=${app.options.projectId}, appId=${app.options.applicationId}")

        val gmsStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
        Log.d("NewsFragment", "GMS status=$gmsStatus (0=SUCCESS)")
        if (gmsStatus != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance()
                .getErrorDialog(requireActivity(), gmsStatus, 9000)
                ?.show()
        }}

    /** Realtime Firestore subscription */
    private fun subscribeToDiet() {
        dietListener?.remove()
        dietListener = db.collection("dietEntries")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snaps, e ->
                // First response -> stop loading, even if error
                setLoading(false)

                if (e != null || snaps == null) {
                    // Optional: keep last shown data; or clear if you prefer
                    // adapterMorning.replaceAll(emptyList()) ... (you currently use notifyDataSetChanged)
                    return@addSnapshotListener
                }
                val all = snaps.documents.mapNotNull { it.toDietEntryOrNull() }
                applyDietData(all)
            }
    }


    /** Map Firestore doc -> DietEntry (defensive) */
    private fun DocumentSnapshot.toDietEntryOrNull(): DietEntry? {
        val name = getString("name") ?: return null
        val calories = (getLong("calories") ?: 0L).toInt()
        val part = getString("partOfDay") ?: "Morning"
        return DietEntry(name = name, calories = calories, partOfDay = part)
    }

    /** Split lists and refresh adapters + summary */
    private fun applyDietData(all: List<DietEntry>) {
        val m = all.filter { it.partOfDay.equals("Morning", ignoreCase = true) }
        val a = all.filter { it.partOfDay.equals("Afternoon", ignoreCase = true) }
        val e = all.filter { it.partOfDay.equals("Evening", ignoreCase = true) }

        morning.clear(); morning.addAll(m); adapterMorning.notifyDataSetChanged()
        afternoon.clear(); afternoon.addAll(a); adapterAfternoon.notifyDataSetChanged()
        evening.clear(); evening.addAll(e); adapterEvening.notifyDataSetChanged()

        updateSummary()
    }

    private fun updateSummary() {
        val total = morning.sumOf { it.calories } +
                afternoon.sumOf { it.calories } +
                evening.sumOf { it.calories }
        tvTotal.text = "Total: $total kcal"
        tvQuality.text = "Quality: ${qualityScore(total)}"
    }

    private fun qualityScore(totalKcal: Int): Int {
        val target = 2000
        val diff = abs(totalKcal - target)
        return max(0, 100 - min(100, diff / 20))
    }
}
