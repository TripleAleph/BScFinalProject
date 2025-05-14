package com.example.pawsitivelife.ui.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawsitivelife.databinding.FragmentHomeBinding
import com.example.pawsitivelife.R


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeCARDAddDog.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_addDogFragment)
        }

        binding.articleCARD1.setOnClickListener {
            findNavController().navigate(R.id.Article1Fragment)
        }

        binding.articleCARD2.setOnClickListener {
            findNavController().navigate(R.id.Article2Fragment)
        }

        binding.articleCARD3.setOnClickListener {
            findNavController().navigate(R.id.Article3Fragment)
        }

        binding.articleCARD4.setOnClickListener {
            findNavController().navigate(R.id.Article4Fragment)
        }

        binding.articleCARD5.setOnClickListener {
            findNavController().navigate(R.id.Article5Fragment)
        }

        binding.articleCARD6.setOnClickListener {
            findNavController().navigate(R.id.Article6Fragment)
        }

        binding.articleCARD7.setOnClickListener {
            findNavController().navigate(R.id.Article7Fragment)
        }

        binding.articleCARD8.setOnClickListener {
            findNavController().navigate(R.id.Article8Fragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}