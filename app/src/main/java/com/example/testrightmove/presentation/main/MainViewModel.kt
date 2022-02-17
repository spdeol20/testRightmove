package com.example.testrightmove.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testrightmove.helper.ReponseHelper
import com.example.testrightmove.model.PropertiesItem
import com.example.testrightmove.util.Connectivity
import com.example.testrightmove.util.Internet
import com.example.testrightmove.util.Loader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
      val connectivity: Connectivity,
    val helper: ReponseHelper
) : ViewModel() {
    private val TAG = "viewmodel"
    private val propertiesStateFlowData = MutableStateFlow<List<PropertiesItem?>?>(null)
    private val averagePrice = MutableStateFlow<Int>(0)

    fun getProperties() = propertiesStateFlowData
    fun getAveragePrice() = averagePrice.asStateFlow()

    init {
        loadProperties()
    }
//    fun internetStatus():MutableStateFlow<Internet.INTERNET_AVAILABLE = connectivity.internetStatus
//
//    fun loadingStatus() = helper.loadingStatus

    fun unregister() = connectivity.unregisterNetworkCallback()


    fun loadProperties() {

        Log.e(TAG, "loadCountries: view modelgetApiData ${connectivity.hasInternet()}")
        if (connectivity.hasInternet()) {
            helper.loading()
            viewModelScope.launch {
                var data = repository.getApiData()
                Log.e(TAG, "loadCountries: getApiData $data")
                if (data?.isSuccessful == true) {
                    helper.success()
                    Log.e(TAG, "loadCountries: success")
                    propertiesStateFlowData.value = data.body()?.properties
                    calculatePrice(data.body()?.properties)
                } else {
                    helper.failure()
                    Log.e(TAG, "loadCountries: fail")

                }
            }
        }else{
            connectivity.noInternet()
        }


    }



    private fun calculatePrice(properties: List<PropertiesItem?>?) {
        var totalPricelist = 0
        val totalHouses = properties?.size ?: 0
        properties?.forEach { item ->
            totalPricelist += item?.price ?: 0
        }
        val average = totalPricelist / totalHouses
        averagePrice.value = (average)
    }
}