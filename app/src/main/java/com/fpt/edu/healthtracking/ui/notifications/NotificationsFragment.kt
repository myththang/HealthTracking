package com.fpt.edu.healthtracking.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.databinding.FragmentNotificationsBinding
import com.fpt.edu.healthtracking.ui.notifications.NotificationAdapter.NotificationViewHolder
import java.util.Arrays

class NotificationsFragment : Fragment() {
    private var binding: FragmentNotificationsBinding? = null
    private lateinit var adapter: NotificationAdapter
    private lateinit var backImageButton: ImageButton
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {


        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        // Setup RecyclerView
        val recyclerView =
            binding!!.recyclerViewNotifications // Make sure this ID matches your layout
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NotificationAdapter()
        recyclerView.adapter = adapter

        // Sample data
        val notifications = Arrays.asList(
            NotificationItem(
                R.drawable.ic_avatar, "First Notification",
                "This is the content of the first notification"
            ),
            NotificationItem(
                R.drawable.ic_avatar, "Second Notification",
                "This is the content of the second notification"
            )
        )
        adapter!!.setNotifications(notifications)
        // Find the ImageButton and set OnClickListener
        backImageButton = root.findViewById(R.id.topImageButton)
        backImageButton.setOnClickListener {
            val navController = findNavController(requireView() )
            navController.navigate(R.id.navigation_home)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

// NotificationItem.java
internal class NotificationItem(val iconResource: Int, val title: String, val content: String)

// NotificationAdapter.java
internal class NotificationAdapter : RecyclerView.Adapter<NotificationViewHolder>() {
    private var notifications: List<NotificationItem> = ArrayList()

    fun setNotifications(notifications: List<NotificationItem>) {
        this.notifications = notifications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.imageView.setImageResource(notification.iconResource)
        holder.titleTextView.text = notification.title
        holder.contentTextView.text = notification.content
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    internal class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView =
            itemView.findViewById(R.id.imageNotification)
        var titleTextView: TextView =
            itemView.findViewById(R.id.txtTitle)
        var contentTextView: TextView =
            itemView.findViewById(R.id.txtContent)
    }
}