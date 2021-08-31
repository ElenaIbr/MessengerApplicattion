package com.example.messengerapplication.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.messengerapplication.MainActivity
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentEnterCodeBinding
import com.example.messengerapplication.utilits.*
import com.google.firebase.auth.PhoneAuthProvider

class EnterCodeFragment(val phoneNum: String, val id: String) : Fragment(R.layout.fragment_enter_code) {
    private var regInputCode: EditText? = null

    private lateinit var binding: FragmentEnterCodeBinding
    private var pressed: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEnterCodeBinding.bind(view)
    }

    override fun onStart() {

        super.onStart()
        regInputCode = binding.regCodeInput
        regInputCode?.requestFocus()

        countdown()
        regInputCode?.addTextChangedListener(AppTextWatcher{
            val str = regInputCode?.text?.toString()
            if(str?.length==6){
                pressed = true
                enterCode()
            }
        })
    }

    private fun enterCode(){
        val credential = PhoneAuthProvider.getCredential(id, regInputCode?.text.toString())
        authFirebase.signInWithCredential(credential)
            .addOnFailureListener {
            Log.d("MyLog", "${showToast(it.message.toString()) }")}
            .addOnCompleteListener {
            if(it.isSuccessful){
                val uid = authFirebase.currentUser?.uid.toString()
                Log.d("MyLog", "$uid")
                val dateMap = mutableMapOf<String, Any>()
                dateMap[CHILD_ID] = uid
                Log.d("MyLog", "${dateMap[CHILD_ID]}d")
                dateMap[CHILD_PHONE] = phoneNum
                Log.d("MyLog", "${dateMap[CHILD_PHONE]}")
                //dateMap[CHILD_USERNAME] = getString(R.string.default_username)

                REF_DATABASE_ROOT.child(NODE_PHONES).child(phoneNum).setValue(uid)
                    .addOnFailureListener { showToast(it.message.toString())
                        Log.d("MyLog", "${it.message.toString()}")
                    }
                    .addOnSuccessListener {
                        Log.d("MyLog", "Tyn")
                        Log.d("MyLog", "$dateMap")
                        REF_DATABASE_ROOT.child(NODE_USERS).child(uid).updateChildren(dateMap)
                            .addOnCompleteListener {
                                //replaceFragment(ChatFragment(), false)
                                //fragmentManager?.beginTransaction()?.remove(this)?.commit()
                                showToast("Добро пожаловать!")
                                APP_ACTIVITY.startOtherActivity(MainActivity())
                            }
                            .addOnFailureListener { showToast(it.message.toString()) }
                    }
            }
            else{
                showToast(it.exception?.message.toString())
            }
        }
    }


    private fun countdown(){
        object : CountDownTimer(59000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if(pressed){
                    cancel()
                }
                binding.chronometer.text = "0:${(millisUntilFinished / 1000)}"
            }
            override fun onFinish() {
                APP_ACTIVITY.startOtherActivity(MainActivity())
            }
        }.start()
    }
}