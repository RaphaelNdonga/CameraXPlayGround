package com.cameraxplayground

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cameraxplayground.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {
    private lateinit var binding: FragmentGalleryBinding

    inner class GalleryViewPagerAdapter(fragment:Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

        override fun createFragment(position: Int): Fragment {
            TODO("Not yet implemented")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(layoutInflater, container, false)

        return binding.root
    }
}