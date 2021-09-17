package com.cameraxplayground

import android.app.AlertDialog
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
            Log.i("GalleryFragment", "The position is $position")
            return PhotoFragment.create(mediaDir[position])
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(layoutInflater, container, false)

        val rootDir = File(galleryArgs.directoryPath)
        val extensionPath = arrayOf("JPG")

        mediaDir = rootDir.listFiles { file ->
            extensionPath.contains(file.extension.uppercase())
        }?.sortedDescending()?.toMutableList() ?: mutableListOf()

        binding.viewPager.adapter = GalleryViewPagerAdapter(this)




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteIcon.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            Log.i("GalleryFragment", "The position is $currentItem")
            mediaDir.getOrNull(currentItem)?.let { mediaFile ->
                AlertDialog.Builder(requireContext())
                    .setPositiveButton("Delete") { _, _ ->
                        mediaFile.delete()

                        //Notify other apps of the deletion
                        MediaScannerConnection.scanFile(
                            requireContext(),
                            arrayOf(mediaFile.absolutePath),
                            null,
                            null
                        )

                        mediaDir.removeAt(currentItem)
                        Log.i("GalleryFragment", "The deleted position is $currentItem")
                        binding.viewPager.adapter?.notifyItemRemoved(currentItem)

                        if (mediaDir.isNullOrEmpty()) {
                            findNavController().navigateUp()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to delete this file?")
                    .create().show()
            }
        }
        binding.shareIcon.setOnClickListener {

        }
    }
}