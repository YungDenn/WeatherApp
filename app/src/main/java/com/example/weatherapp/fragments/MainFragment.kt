package com.example.weatherapp.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.ClientError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.DialogManager
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.R
import com.example.weatherapp.adpters.VpAdapter
import com.example.weatherapp.adpters.WeatherModel
import com.example.weatherapp.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import kotlin.math.max


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var fLocationClient: FusedLocationProviderClient
    private val model: MainViewModel by activityViewModels()

    private val fragmentList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )

    private val nameList = listOf(
        "Hours",
        "Days"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fragmentList)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = nameList[position]
        }.attach()

        tbSync.setOnClickListener {
            checkLocation()
            tabLayout.selectTab(tabLayout.getTabAt(0))
        }

        tbSearch.setOnClickListener{
            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener{
                override fun onClickSettings(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }
            })
        }
    }

    private fun checkLocation() {
        if (isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object: DialogManager.Listener{
                override fun onClickSettings(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }


    private fun isLocationEnabled(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        val cancellationToken = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnCompleteListener {
            requestWeatherData("${it.result.latitude}, ${it.result.longitude}")
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "http://api.weatherapi.com/v1/forecast.json?" +
                "key=$API_KEY&" +
                "q=$city&" +
                "days=3&" +
                "aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseWeatherData(result)
            },
            { error ->
                Toast.makeText(requireContext(), "City not found",Toast.LENGTH_SHORT).show()
                Log.d("MyLog", "Error: $error")
            }
        )
        queue.add(request)
    }

    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemp = "${it.maxTemp}°/${it.minTemp}°"
            tvData.text = it.time
            tvCityName.text = it.city
            tvCurrentTemp.text = it.currentTemp.ifEmpty { maxMinTemp }
            tvCondition.text = it.condition
            tvMaxMinTemp.text = if (it.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load("https:${it.imageUrl}").into(imWeather)
        }
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])

    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject
            .getJSONObject("forecast")
            .getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c"),
                day.getJSONObject("day").getString("mintemp_c"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }


    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            weatherItem.hours
        )
        model.liveDataCurrent.value = item
        Log.d(
            "MyLog",
            "Result: City: ${item.city} Time: ${item.time}, Current: ${item.currentTemp}"
        )
    }

    companion object {

        private const val API_KEY = "cfb99916f27a4070a0154533222010"

        @JvmStatic
        fun newInstance() = MainFragment().apply {

        }
    }
}
