package com.example.pawsitivelife.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R
import com.example.pawsitivelife.adapter.VetAdapter
import com.example.pawsitivelife.model.Vet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VetListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vet_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewVets)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadGlobalVets { vets ->
            recyclerView.adapter = VetAdapter(vets) { selectedVet ->
                Toast.makeText(requireContext(), "Selected: ${selectedVet.name}", Toast.LENGTH_SHORT).show()
            }
        }

        val btnSelectVet = view.findViewById<Button>(R.id.dogVet_BTN_edit)
        btnSelectVet.setOnClickListener {
            fetchVetsAndShowDialog()
        }

    }

    /*private fun loadGlobalVets(onResult: (List<Vet>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("vets").get()
            .addOnSuccessListener { result ->
                val vets = result.mapNotNull { it.toObject(Vet::class.java) }
                onResult(vets)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load vets", Toast.LENGTH_SHORT).show()
            }
    }*/

    private fun loadGlobalVets(onResult: (List<Vet>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("vets").get()
            .addOnSuccessListener { result ->
                val vets = result.mapNotNull {
                    val vet = it.toObject(Vet::class.java)
                    Log.d("VetList", "Loaded vet: ${vet}")
                    vet
                }
                Log.d("VetList", "Total loaded: ${vets.size}")
                onResult(vets)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load vets", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchVetsAndShowDialog() {
        val db = Firebase.firestore
        val vetList = mutableListOf<Vet>()

        db.collection("vets")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val vet = document.toObject(Vet::class.java)
                    vetList.add(vet)
                }

                showVetSelectionDialog(vetList)
            }
            .addOnFailureListener { exception ->
                Log.w("VetList", "Error getting vets.", exception)
            }
    }

    private fun showVetSelectionDialog(vets: List<Vet>) {
        val vetNames = vets.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select a Vet")
            .setItems(vetNames) { dialog, which ->
                val selectedVet = vets[which]
                Toast.makeText(requireContext(), "Selected: ${selectedVet.name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
