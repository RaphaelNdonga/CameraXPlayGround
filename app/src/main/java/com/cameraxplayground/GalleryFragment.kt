package com.cameraxplayground

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cameraxplayground.databinding.FragmentGalleryBinding
import java.io.File

class GalleryFragment : Fragment() {
    private lateinit var binding: FragmentGalleryBinding
    private val galleryArgs by navArgs<GalleryFragmentArgs>()
    private lateinit var mediaDir: MutableList<File>

    inner class GalleryViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = mediaDir.size

        override fun createFragment(position: Int): Fragment {
            return mediaDir[position].let { PhotoFragment.create(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(layoutInflater, container, false)

        val rootDir = File(galleryArgs.directoryPath)
        val extensionPath = arrayOf("jpg")

        mediaDir = rootDir.listFiles { file->
            extensionPath.contains(file.extension)
        }?.sortedDescending()?.toMutableList()?: mutableListOf()

        binding.viewPager.adapter = GalleryViewPagerAdapter(this)




        return binding.root
    }
}