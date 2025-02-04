package com.fpt.edu.healthtracking.util.file

import android.content.Context
import android.net.Uri
import java.io.File

class ImageUtils {

    fun setImage(context: Context){

}

    companion object {
        fun setImage(requireContext: Context): Uri {
            val sharedPreferences = requireContext.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            val profileImagePath = sharedPreferences?.getString("profile_image_path", null)

            profileImagePath?.let {
                val file = File(it)
                if (file.exists()) {
                    return Uri.fromFile(file)  // Load the profile image into ImageView
                }
            }
            return Uri.EMPTY
        }
    }
}