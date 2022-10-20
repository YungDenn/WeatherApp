package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.fragments.MainFragment
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.placeHolder, MainFragment.newInstance()).commit()

//        binding.getButton.setOnClickListener{
//            getResult("London")
//        }
    }

    private fun getResult(city: String) {
        val url = "http://api.weatherapi.com/v1/current.json?" +
                "key=$API_KEY&q=$city&aqi=no"


        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET,
            url,
            { response ->
                val obj = JSONObject(response)
                val temp = obj.getJSONObject("current")
                Log.d("MyLog", "Volley message: ${temp.getString("temp_c")}")
            },
            { Log.d("MyLog", "Volley Error: $it") })
        queue.add(stringRequest)
    }

    companion object {
        const val API_KEY = "373cf141496f4d23a82110519222609"
    }
}