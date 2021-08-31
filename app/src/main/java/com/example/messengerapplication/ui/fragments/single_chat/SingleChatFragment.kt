package com.example.messengerapplication.ui.fragments.single_chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentSingleChatBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.models.User
import com.example.messengerapplication.utilits.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue


class SingleChatFragment(val contact: CommonModel) : Fragment(R.layout.fragment_single_chat) {

    private lateinit var binding: FragmentSingleChatBinding
    private var act: View? = null
    private lateinit var headInfoListener: AppValueEventListener
    private lateinit var receivingUser: User
    private lateinit var userRef: DatabaseReference

    private lateinit var messagesRef: DatabaseReference
    private lateinit var adapter: SingleChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageListener: AppValueEventListener

    private var listMessages = emptyList<CommonModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSingleChatBinding.bind(view)
    }

    override fun onResume() {
        super.onResume()

        act = activity?.findViewById(R.id.bottomNav)
        act?.visibility = View.GONE

        headInfoListener = AppValueEventListener {
            receivingUser = it.getUser()
            updateToolbarInfo()
        }

        userRef = REF_DATABASE_ROOT.child(NODE_USERS).child(contact.id)
        userRef.addValueEventListener(headInfoListener)

        binding.backBtn.setOnClickListener {
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }

        binding.sendMessageBtn.setOnClickListener {
            initHeaderInfo()
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {

        recyclerView = binding.rcMessage
        adapter = SingleChatAdapter()
        messagesRef = REF_DATABASE_ROOT.child(NODE_MESSAGES)
            .child(UID)
            .child(contact.id)
        recyclerView.adapter = adapter
        messageListener = AppValueEventListener { dataSnapshot ->
            listMessages = dataSnapshot.children.map { it.getCommonModel() }
            adapter.setList(listMessages)
        }
        messagesRef.addValueEventListener(messageListener)
    }

    private fun initHeaderInfo() {
        val message = binding.chatInputMessage.text.toString()
        if (message.isEmpty()) {
            showToast("Введите текст!")
        } else sentMessage(message, contact.id, TYPE_TEXT) {
            binding.chatInputMessage.setText("")
        }
    }

    private fun sentMessage(message: String,
                            receivingUserID: String,
                            typeText: String,
                            function: () -> Unit) {
        val refDialogUser = "$NODE_MESSAGES/$UID/$receivingUserID"
        val refReceivDialogUser = "$NODE_MESSAGES/$receivingUserID/$UID"

        val messageKey = REF_DATABASE_ROOT.child(refDialogUser).push().key

        val mapMessage = hashMapOf<String, Any>()
        mapMessage[CHILD_FROM] = UID
        mapMessage[CHILD_TYPE] = typeText
        mapMessage[CHILD_TEXT] = message
        mapMessage[CHILD_TIMESTAMP] = ServerValue.TIMESTAMP

        val mapDialod = hashMapOf<String, Any>()
        mapDialod["$refDialogUser/$messageKey"] = mapMessage
        mapDialod["$refReceivDialogUser/$messageKey"] = mapMessage

        REF_DATABASE_ROOT
            .updateChildren(mapDialod)
            .addOnSuccessListener { function() }
            .addOnFailureListener { showToast(it.message.toString()) }
    }

    private fun updateToolbarInfo() {
        binding.toolbarImg.setImg(receivingUser.photoUrl)

        if(receivingUser.fullname.isEmpty()){
            binding.toolbarContactName.text = contact.fullname
        }else binding.toolbarContactName.text = receivingUser.username

        binding.toolbarContactStatus.text = receivingUser.state
    }


    override fun onPause() {
        super.onPause()
        act?.visibility = View.VISIBLE
        userRef.removeEventListener(headInfoListener)
        messagesRef.removeEventListener(messageListener)
    }
}