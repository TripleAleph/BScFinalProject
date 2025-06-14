package com.example.pawsitivelife.ui.home

import com.example.pawsitivelife.ui.home.DogCardAdapter
import com.example.pawsitivelife.ui.home.FilterBottomSheetFragment
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
import com.example.pawsitivelife.adapter.ReminderAdapter
import com.example.pawsitivelife.databinding.FragmentHomeBinding
import com.example.pawsitivelife.model.Reminder
import com.example.pawsitivelife.ui.mydogs.Dog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime

class HomeFragment : Fragment(), FilterBottomSheetFragment.FilterListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private var currentSelectedAges: List<String> = emptyList()
    private var currentSelectedTags: List<String> = emptyList()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var classificationDone = false
    private lateinit var reminderAdapter: ReminderAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDogsFromFirestore()
        logAllDogsOfCurrentUser()
        setupRemindersRecyclerView()
        loadUpcomingRemindersFromFirestore()

        binding.homeBTNFilterArticles.setOnClickListener {
            if (!classificationDone) {
                Toast.makeText(
                    requireContext(),
                    "Please wait, loading articles...",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

//            val sheet = FilterBottomSheetFragment(
//                preselectedTags = currentSelectedTags,
//                preselectedAges = emptyList(),
//                listener = this
//            )

            val sheet = FilterBottomSheetFragment(
                preselectedTags = currentSelectedTags,
                preselectedAges = currentSelectedAges,
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

//    override fun onFiltersApplied(selectedTags: List<String>, selectedAges: List<String>) {
//        currentSelectedTags = (selectedTags + selectedAges).map { it.lowercase() }
//        Log.d("FILTER_TEST", "🎯 Selected tags: $currentSelectedTags")
//
//        val filteredArticles = filterArticles(currentSelectedTags)
//        Log.d("FILTER_TEST", "📰 Filtered articles count: ${filteredArticles.size}")
//
//        showFilteredArticles(filteredArticles)
//    }


    override fun onFiltersApplied(selectedTags: List<String>, selectedAges: List<String>) {
        currentSelectedTags = selectedTags.map { it.lowercase() }
        currentSelectedAges = selectedAges.map { it.lowercase() }

        val combined = (currentSelectedTags + currentSelectedAges).distinct()
        val filteredArticles = filterArticles(combined)
        showFilteredArticles(filteredArticles)
    }

    private fun setupRemindersRecyclerView() {
        reminderAdapter = ReminderAdapter(
            onReminderClick = { reminder ->
                val bundle = Bundle().apply {
                    putString("selectedDate", reminder.date.toLocalDate().toString())
                }
                findNavController().navigate(
                    R.id.action_navigation_home_to_navigation_appointments,
                    bundle
                )
            }
        )
        binding.homeLSTReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.homeLSTReminders.adapter = reminderAdapter
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

//    private fun filterArticles(selectedTags: List<String>): List<Article> {
//        if (selectedTags.isEmpty()) return ArticleRepository.articles
//
//        return ArticleRepository.articles.filter { article ->
//            article.tags.any { tag -> selectedTags.contains(tag.lowercase()) }
//        }
//    }


    //instaed of 5 articles in "seniors" it shows 4
    private fun filterArticles(selectedTags: List<String>): List<Article> {
        if (selectedTags.isEmpty()) return ArticleRepository.articles

        val selectedLower = selectedTags.map { it.lowercase() }

        return ArticleRepository.articles.filter { article ->
            article.tags.any { tag -> selectedLower.contains(tag.lowercase()) }
        }
    }


    private fun loadDogsFromFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("DogLogger", "No user is currently logged in")
            Toast.makeText(
                requireContext(),
                "Please check your internet connection and try again",
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigate(R.id.action_navigation_home_to_signInActivity)
            return
        }

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
                        imageUrl = document.getString("imageUrl") ?: "",
                        dogId = document.id,
                        isMine = document.getBoolean("isMine") ?: false
                    )
                }
                showDogs(dogList)
            }
            .addOnFailureListener { e ->
                Log.e("DogLogger", "Failed to load dogs from Firestore", e)
                Toast.makeText(
                    requireContext(),
                    "Network error: Please check your internet connection and try again",
                    Toast.LENGTH_LONG
                ).show()
                showDogs(emptyList())
            }
    }

    private fun showDogs(dogList: List<Dog>) {
        val adapter = DogCardAdapter(
            dogs = dogList,
            onDogClick = { dog ->
                val bundle = Bundle().apply {
                    putString("dogId", dog.dogId) // Required for Firestore updates
                    putString("name", dog.name)
                    putString("dateOfBirth", dog.dateOfBirth)
                    putString("gender", dog.gender)
                    putString("imageUrl", dog.imageUrl)
                    putString("breed", dog.breed)
                    putString("color", dog.color)
                    putBoolean("neutered", dog.neutered)
                    putBoolean("microchipped", dog.microchipped)
                    putBoolean("isMine", dog.isMine)
                }
                findNavController().navigate(
                    R.id.action_navigation_home_to_dogProfileFragment,
                    bundle
                )
            },
            onAddDogClick = {
                findNavController().navigate(R.id.action_navigation_home_to_addDogFragment)
            }
        )

        binding.homeLSTDogCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.homeLSTDogCards.adapter = adapter
    }

    private fun logAllDogsOfCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("users").document(user.uid).collection("dogs")
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
                        val imageUrl = document.getString("imageUrl") ?: ""
                        val dogId = document.id

                        Log.d(
                            "DogLogger", """
                                🐶 Dog:
                                - ID: $dogId
                                - Name: $name
                                - Breed: $breed
                                - Gender: $gender
                                - Date of Birth: $birth
                                - Color: $color
                                - Neutered: $neutered
                                - Microchipped: $microchipped
                                - Image URL: $imageUrl
                            """.trimIndent()
                        )
                    }
                }
            }
            .addOnFailureListener {
                Log.e("DogLogger", "Failed to fetch dogs", it)
            }
    }

    private fun loadUpcomingRemindersFromFirestore() {
        val user = auth.currentUser ?: return
        val start = LocalDateTime.now()
        val end = start.plusDays(7)
        val reminders = mutableListOf<Reminder>()

        db.collection("users").document(user.uid).collection("dogs")
            .get()
            .addOnSuccessListener { dogs ->
                val dogIds = dogs.map { it.id }
                var pending = dogIds.size

                if (pending == 0) {
                    showNoRemindersMessage()
                    return@addOnSuccessListener
                }

                dogIds.forEach { dogId ->
                    db.collection("users").document(user.uid)
                        .collection("dogs").document(dogId)
                        .collection("reminders")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            for (doc in snapshot) {
                                val title = doc.getString("title") ?: continue
                                val dateStr = doc.getString("date") ?: continue
                                val imagePath = doc.getString("imagePath")
                                    ?: "android.resource://${requireContext().packageName}/${R.drawable.missing_img_dog}"
                                val dogName = doc.getString("dogName") ?: "Unknown"
                                val notes = doc.getString("notes") ?: ""

                                val dateTime = try {
                                    LocalDateTime.parse(dateStr)
                                } catch (e: Exception) {
                                    continue
                                }

                                if (dateTime.isAfter(start.minusSeconds(1)) && dateTime.isBefore(
                                        end.plusSeconds(
                                            1
                                        )
                                    )
                                ) {
                                    val reminder = Reminder(
                                        title = title,
                                        date = dateTime,
                                        dogId = dogId,
                                        dogName = dogName,
                                        imagePath = imagePath,
                                        notes = notes
                                    )
                                    reminders.add(reminder)
                                }
                            }

                            pending--
                            if (pending == 0) {
                                showReminders(reminders)
                            }
                        }
                        .addOnFailureListener {
                            pending--
                            if (pending == 0) {
                                showReminders(reminders)
                            }
                        }
                }
            }
    }

    // 🔄 חדש
    private fun showReminders(reminders: List<Reminder>) {
        if (reminders.isEmpty()) {
            showNoRemindersMessage()
        } else {
            reminderAdapter.submitList(reminders.sortedBy { it.date })

            binding.homeCARDRemindersContainer.visibility = View.VISIBLE
            binding.homeLSTReminders.visibility = View.VISIBLE
            binding.homeLBLEmptyReminders.visibility = View.GONE
        }
    }

    private fun showNoRemindersMessage() {
        binding.homeCARDRemindersContainer.visibility = View.VISIBLE
        binding.homeLSTReminders.visibility = View.GONE
        binding.homeLBLEmptyReminders.visibility = View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
