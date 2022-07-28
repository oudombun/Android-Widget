package com.oudombun.widgettesting

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.oudombun.widgettesting.databinding.ActivityMainBinding
import com.oudombun.widgettesting.databinding.QrWidgetConfigureBinding
import com.oudombun.widgettesting.event.RxBus
import com.oudombun.widgettesting.event.RxEvent
import io.reactivex.rxjava3.disposables.Disposable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //This code should be removed when authenticate is implemented
        checkAuthenticate()
    }

    private fun checkAuthenticate() {
        binding.isLogin = isLogin

        binding.btnLogin.setOnClickListener {
            isLogin=true
            binding.btnLogout.visibility=View.VISIBLE
            binding.btnLogin.visibility=View.GONE
            RxBus.publish(RxEvent.EventLogin())
        }

        binding.btnLogout.setOnClickListener {
            isLogin=false
            binding.btnLogin.visibility=View.VISIBLE
            binding.btnLogout.visibility=View.GONE
            RxBus.publish(RxEvent.EventLogout())
        }

    }

    companion object{
        var isLogin = true
    }
}