package com.cameraxplayground

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import java.io.File

class PhotoFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ImageView(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val resource = arguments?.getString(FILE_NAME_KEY)?.let {
            File(it)
        }
        Glide.with(view).load(resource).into(view as ImageView)
    }

    companion object{
        private const val FILE_NAME_KEY: String = "file_name"

        fun create(file:File):Fragment{
            return PhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(FILE_NAME_KEY,file.absolutePath)
                }
            }
        }
    }
}