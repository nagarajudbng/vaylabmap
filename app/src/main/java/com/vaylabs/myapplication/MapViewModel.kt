//package com.vaylabs.myapplication
//
//import android.content.Context
//import android.location.Address
//import android.location.Geocoder
//import android.os.Build
//import android.os.Build.VERSION.SDK_INT
//import android.util.Log
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.codelabs.foodadminapp.feature_map.data.MapRepository
//import com.codelabs.foodadminapp.feature_map.data.model.response.MapBoundsResponse
//import com.codelabs.foodadminapp.feature_map.util.ResponseState
//import com.google.android.gms.maps.model.LatLng
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import java.util.Locale
//import javax.inject.Inject
//
//class MapViewModel  (   private val mapRepository: MapRepository) : ViewModel() {
//
//
//    private val _recomposeState = MutableStateFlow<Boolean>(false)
//    val recomposeState = _recomposeState.asStateFlow()
//
//    private val _markers = mutableStateOf<List<LatLng>>(emptyList())
//    val markers get() = _markers.value
//
//
//    private val _markerAddressDetail = MutableStateFlow<ResponseState<Address>>(ResponseState
//        .Idle(""))//ResponseState is a wrapper class
//    val markerAddressDetail = _markerAddressDetail.asStateFlow()
//
//    private val _boundsDetails = MutableStateFlow<MapBoundsResponse>(MapBoundsResponse())
//    val boundsDetails = _boundsDetails.asStateFlow()
//
//    private val _southWest = MutableStateFlow<LatLng>(LatLng(0.0, 0.0))
//    val southWest = _southWest.asStateFlow()
//
//    private val _northEast = MutableStateFlow<LatLng>(LatLng(0.0, 0.0))
//    val northEast = _northEast.asStateFlow()
//
//    fun getMarkerAddressDetails(lat: Double, long: Double, context: Context) {
//        _markerAddressDetail.value = ResponseState.Loading("")//We will show loading first
//        try {
//            //Not a good practice to pass context in vm, instead inject this Geocoder
//            getBoundsDetails(lat, long, context)
//            val geocoder = Geocoder(context, Locale.getDefault())
//            if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                geocoder.getFromLocation(//Pass LatLng and get address
//                    lat,
//                    long,
//                    1,//no. of addresses you want
//                ) { p0 ->
//                    Log.d("MapViewModel 1", "getMarkerAddressDetails: $p0")
//                    _markerAddressDetail.value = ResponseState.Success(p0[0])
//                }
//            } else {
//                val addresses = geocoder.getFromLocation(//This method is deprecated for >32
//                    lat,
//                    long,
//                    1,
//                )
//                Log.d("MapViewModel 2", "getMarkerAddressDetails: $addresses")
//                _markerAddressDetail.value =
//                    if(!addresses.isNullOrEmpty()){//The address can be null or empty
//                        Log.d("MapViewModel 2 address", "getMarkerAddressDetails: ${addresses[0]}")
//                        ResponseState.Success(addresses[0])
//
//                    }else{
//                        ResponseState.Error(Exception("Address is null"))
//                    }
//            }
//        } catch (e: Exception) {
//            Log.d("MapViewModel", "getMarkerAddressDetails: $e")
//            _markerAddressDetail.value = ResponseState.Error(e)
//        }
//    }
//    fun getBoundsDetails(lat: Double, long: Double, context: Context) {
//        viewModelScope.launch {
//            Log.d("MapViewModel", "getBoundsDetails: $lat $long")
//            _boundsDetails.value = mapRepository.getBounds(lat, long)
//            _recomposeState.value = !recomposeState.value
//            Log.d("MapViewModel", "getBoundsDetails: ${_boundsDetails.value}")
//            val latLngList = mutableListOf<LatLng>()
//            if(_boundsDetails.value.results.isNullOrEmpty()){
//                _boundsDetails.value.results.forEach{
//                    Log.d("MapViewModel", "getBoundsDetails: ${it.geometry?.bounds}")
//                    var sw = _boundsDetails.value.results.first().geometry?.bounds?.southwest?.let {
//                        it.lat?.let { it1 ->
//                            it.lng?.let { it2 ->
//                                Log.d("MapViewModel", "getBoundsDetails 1: $it1 $it2")
//                                LatLng(
//                                    it1,
//                                    it2
//                                )
//                            }
//                        }
//                    }!!
//                   var ne = _boundsDetails.value.results.first().geometry?.bounds?.northeast?.let {
//                        it.lat?.let { it1 ->
//                            it.lng?.let { it2 ->
//                                Log.d("MapViewModel", "getBoundsDetails 2: $it1 $it2")
//                                LatLng(
//                                    it1,
//                                    it2
//                                )
//                            }
//                        }
//                    }!!
//                    latLngList.add(sw)
//                    latLngList.add(ne)
//                }
//            }
//            _markers.value = latLngList
//            _southWest.value = _boundsDetails.value.results.first().geometry?.bounds?.southwest?.let {
//                it.lat?.let { it1 ->
//                    it.lng?.let { it2 ->
//                        Log.d("MapViewModel", "getBoundsDetails 1: $it1 $it2")
//                        LatLng(
//                            it1,
//                            it2
//                        )
//                    }
//                }
//            }!!
//            _northEast.value = _boundsDetails.value.results.first().geometry?.bounds?.northeast?.let {
//                it.lat?.let { it1 ->
//                    it.lng?.let { it2 ->
//                        Log.d("MapViewModel", "getBoundsDetails 2: $it1 $it2")
//                        LatLng(
//                            it1,
//                            it2
//                        )
//                    }
//                }
//            }!!
//
//        }
//    }
//}