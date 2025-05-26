package com.example.pawsitivelife.ui.home

import com.example.pawsitivelife.model.Article
import com.example.pawsitivelife.api.CohereService
import com.example.pawsitivelife.dataDogs.ArticleRepository
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.FragmentHomeBinding
import com.example.pawsitivelife.ui.mydogs.Dog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), FilterBottomSheetFragment.FilterListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentSelectedTags: List<String> = emptyList()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var classificationDone = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUsername()
        loadDogsFromFirestore()
        logAllDogsOfCurrentUser()

        binding.homeBTNFilterArticles.setOnClickListener {
            if (!classificationDone) {
                Toast.makeText(requireContext(), "Please wait, loading articles...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sheet = FilterBottomSheetFragment(
                preselectedTags = currentSelectedTags,
                preselectedAges = emptyList(),
                listener = this
            )
            sheet.show(parentFragmentManager, sheet.tag)
        }

        val total = ArticleRepository.articles.size
        var classifiedCount = 0

        ArticleRepository.articles.forEach { article ->
            CohereService.classifyArticleAge(article.content) { result ->
                if (result != null) {
                    val ageTags = result.split(",").map { it.trim().lowercase() }
                    article.ageCategory = ageTags
                    article.tags = (article.tags + ageTags).map { it.lowercase() }.distinct()

                    Log.d("AI_RESPONSE", "✔️ '${article.title}' → Age: $ageTags")
                    Log.d("DEBUG_TAGS", "📄 ${article.title} → ${article.tags}")
                } else {
                    Log.e("AI_RESPONSE", "❌ Failed to classify article: ${article.title}")
                }

                classifiedCount++
                if (classifiedCount == total) {
                    classificationDone = true
                    val filtered = filterArticles(currentSelectedTags)
                    showFilteredArticles(filtered)
                }
            }
        }

        binding.articleCARD1.setOnClickListener { findNavController().navigate(R.id.Article1Fragment) }
        binding.articleCARD2.setOnClickListener { findNavController().navigate(R.id.Article2Fragment) }
        binding.articleCARD3.setOnClickListener { findNavController().navigate(R.id.Article3Fragment) }
        binding.articleCARD4.setOnClickListener { findNavController().navigate(R.id.Article4Fragment) }
        binding.articleCARD5.setOnClickListener { findNavController().navigate(R.id.Article5Fragment) }
        binding.articleCARD6.setOnClickListener { findNavController().navigate(R.id.Article6Fragment) }
        binding.articleCARD7.setOnClickListener { findNavController().navigate(R.id.Article7Fragment) }
        binding.articleCARD8.setOnClickListener { findNavController().navigate(R.id.Article8Fragment) }
    }

    override fun onFiltersApplied(selectedTags: List<String>, selectedAges: List<String>) {
        currentSelectedTags = (selectedTags + selectedAges).map { it.lowercase() }
        Log.d("FILTER_TEST", "🎯 Selected tags: $currentSelectedTags")

        val filteredArticles = filterArticles(currentSelectedTags)
        Log.d("FILTER_TEST", "📰 Filtered articles count: ${filteredArticles.size}")

        showFilteredArticles(filteredArticles)
    }

    private fun showFilteredArticles(articles: List<Article>) {
        val articleMap = mapOf(
            "How Many Times a Day Should a Dog Eat?" to binding.articleCARD1,
            "Should You Give a Dog as a Gift?" to binding.articleCARD2,
            "What to Do if Your Dog Has Dry Skin" to binding.articleCARD3,
            "Your Puppy’s Diet & Nutritional Needs" to binding.articleCARD4,
            "What’s Causing My Puppy’s Upset Stomach?" to binding.articleCARD5,
            "Why Do Dogs Have Itchy Ears?" to binding.articleCARD6,
            "Puppy Training: How & When to Potty Train a Puppy" to binding.articleCARD7,
            "How Often to Feed a Puppy?" to binding.articleCARD8
        )

        articleMap.values.forEach { it.visibility = View.GONE }

        articles.forEach { article ->
            articleMap[article.title]?.let {
                it.visibility = View.VISIBLE
            }
        }

        Log.d("ARTICLE_FILTERED", "✅ Showing articles: ${articles.map { it.title }}")
    }

    private fun filterArticles(selectedTags: List<String>): List<Article> {
        if (selectedTags.isEmpty()) return ArticleRepository.articles

        return ArticleRepository.articles.filter { article ->
            article.tags.any { tag -> selectedTags.contains(tag.lowercase()) }
        }
    }

    private fun loadUsername() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "unknown"
                binding.homeLBLUsername.text = "@$username"
            }
            .addOnFailureListener {
                binding.homeLBLUsername.text = "@error"
            }
    }

    private fun loadDogsFromFirestore() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("dogs")
            .get()
            .addOnSuccessListener { result ->
                val dogList = result.map { document ->
                    Dog(
                        name = document.getString("name") ?: "",
                        breed = document.getString("breed") ?: "",
                        dateOfBirth = document.getString("dateOfBirth") ?: "-",
                        gender = document.getString("gender") ?: "",
                        color = document.getString("color") ?: "",
                        neutered = document.getBoolean("neutered") ?: false,
                        microchipped = document.getBoolean("microchipped") ?: false,
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                }
                showDogs(dogList)
            }
            .addOnFailureListener {
                showDogs(emptyList())
            }
    }

    private fun showDogs(dogList: List<Dog>) {
        val adapter = DogCardAdapter(
            dogs = dogList,
            onDogClick = { dog ->
                val bundle = Bundle().apply {
                    putString("name", dog.name)
                    putString("dateOfBirth", dog.dateOfBirth)
                    putString("gender", dog.gender)
                    putString("imageUrl", dog.imageUrl)
                    putString("breed", dog.breed)
                    putString("color", dog.color)
                    putBoolean("neutered", dog.neutered)
                    putBoolean("microchipped", dog.microchipped)
                }
                findNavController().navigate(R.id.action_navigation_home_to_dogProfileFragment, bundle)
            },
            onAddDogClick = {
                findNavController().navigate(R.id.action_navigation_home_to_addDogFragment)
            }
        )

        binding.homeLSTDogCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.homeLSTDogCards.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logAllDogsOfCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(user.uid)
            .collection("dogs")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("DogLogger", "No dogs found for user ${user.uid}")
                } else {
                    Log.d("DogLogger", "Dogs for user ${user.uid}:")
                    for (document in result) {
                        val name = document.getString("name") ?: "Unnamed"
                        val breed = document.getString("breed") ?: "Unknown"
                        val gender = document.getString("gender") ?: "-"
                        val birth = document.getString("dateOfBirth") ?: "-"
                        val color = document.getString("color") ?: "-"
                        val neutered = document.getBoolean("neutered") ?: false
                        val microchipped = document.getBoolean("microchipped") ?: false

                        Log.d("DogLogger", """
                            🐶 Dog:
                            - Name: $name
                            - Breed: $breed
                            - Gender: $gender
                            - Date of Birth: $birth
                            - Color: $color
                            - Neutered: $neutered
                            - Microchipped: $microchipped
                        """.trimIndent())
                    }
                }
            }
            .addOnFailureListener {
                Log.e("DogLogger", "Failed to fetch dogs", it)
            }
    }
}