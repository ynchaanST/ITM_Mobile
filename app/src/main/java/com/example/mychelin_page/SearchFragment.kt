package com.example.mychelin_page

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlin.math.pow
import kotlin.math.sqrt

class SearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var searchBar: EditText
    private lateinit var searchButton: Button
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val markers = mutableListOf<Marker>()
    private val db = FirebaseFirestore.getInstance()
    private val geocodeApiKey = "VKFgdhKpLwYMLWZEh81rgN87eForBC12XEq6CRzb" // 네이버 API 키

    private lateinit var voiceSearchButton: Button
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)

        mapView = rootView.findViewById(R.id.map_view)
        searchBar = rootView.findViewById(R.id.search_bar)
        searchButton = rootView.findViewById(R.id.search_button)
        voiceSearchButton = rootView.findViewById(R.id.voice_search_button)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim()
            if (query.isNotEmpty()) {
                searchRestaurants(query)
            } else {
                Toast.makeText(requireContext(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        voiceSearchButton.setOnClickListener {
            startVoiceRecognition()
        }

        setupSpeechRecognizer()

        return rootView
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        // 마커 클릭 이벤트 설정

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Spinner 초기화
        setupFilterSpinner()
    }

    private fun searchRestaurants(query: String) {
        db.collection("restaurants").get()
            .addOnSuccessListener { documents ->
                val filteredRestaurants = documents.filter { doc ->
                    val name = doc.getString("name") ?: ""
                    name.contains(query, ignoreCase = true)
                }.map { doc ->
                    val name = doc.getString("name") ?: ""
                    val roadAddress = doc.getString("road_address") ?: ""
                    Restaurants(name, roadAddress)
                }

                if (filteredRestaurants.isEmpty()) {
                    Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    geocodeAndUpdateMarkers(filteredRestaurants)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to fetch restaurants", e)
                Toast.makeText(requireContext(), "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun geocodeAndUpdateMarkers(restaurants: List<Restaurants>) {
        clearAllMarkers()

        val client = OkHttpClient()
        for (restaurant in restaurants) {
            val request = Request.Builder()
                .url("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=${restaurant.roadAddress}")
                .addHeader("X-NCP-APIGW-API-KEY-ID", "8ncq4hnswu")
                .addHeader("X-NCP-APIGW-API-KEY", geocodeApiKey)
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("GeocodeError", "Failed to geocode address: ${restaurant.roadAddress}", e)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val json = JSONObject(responseBody ?: "")
                        val addresses = json.getJSONArray("addresses")
                        if (addresses.length() > 0) {
                            val address = addresses.getJSONObject(0)
                            val latitude = address.getDouble("y")
                            val longitude = address.getDouble("x")
                            val latLng = LatLng(latitude, longitude)

                            // UI 작업은 메인 스레드에서 실행해야 함
                            requireActivity().runOnUiThread {
                                val marker = Marker()
                                marker.position = latLng
                                marker.captionText = restaurant.name
                                marker.map = naverMap
                                markers.add(marker)

                                for (marker in markers) {
                                    marker.setOnClickListener {
                                        showRestaurantInfoDialog(marker.captionText ?: "", latLng)
                                        true
                                    }
                                }

                                // 마커 추가 후 가장 가까운 마커로 카메라 이동
                                if (markers.isNotEmpty()) {
                                    val currentLocation = naverMap.locationOverlay.position
                                    val nearestMarker = markers.minByOrNull { marker ->
                                        marker.position.distanceTo(currentLocation)
                                    }
                                    nearestMarker?.let {
                                        val cameraUpdate = CameraUpdate.scrollTo(it.position).animate(CameraAnimation.Easing)
                                        naverMap.moveCamera(cameraUpdate)
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e("GeocodeError", "Response not successful: ${response.code}")
                    }
                }
            })
        }
    }

    private fun showRestaurantInfoDialog(restaurantName: String, latLng: LatLng) {
        val initialDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(restaurantName)
            .setMessage("옵션을 선택하세요:")
            .setPositiveButton("식당 정보 입력") { _, _ ->
                // 식당 정보 입력 Dialog 표시
                showRestaurantDetailsDialog(restaurantName, latLng)
            }
            .setNegativeButton("예약") { _, _ ->
                // 예약 버튼 클릭 시 Toast 메시지 표시
//                Toast.makeText(requireContext(), "아직 예약 정보 구현이 되지 않았습니다.", Toast.LENGTH_SHORT).show()
                navigateToTableSelection(restaurantName, latLng)
            }
            .create()

        initialDialog.show()
    }


    private fun showRestaurantDetailsDialog(restaurantName: String, latLng: LatLng) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_restaurant_details, null)
        val restaurantNameField = dialogView.findViewById<EditText>(R.id.input_restaurant_name)
        val menuField = dialogView.findViewById<EditText>(R.id.input_menu)
        val priceField = dialogView.findViewById<EditText>(R.id.input_price)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.rating_bar)
        val visitCountField = dialogView.findViewById<EditText>(R.id.input_visit_count)
        val totalSpentField = dialogView.findViewById<EditText>(R.id.input_total_spent)
        val submitButton = dialogView.findViewById<Button>(R.id.submit_button)

        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다. 로그인 후 다시 시도하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = user.uid

        restaurantNameField.setText(restaurantName)
        visitCountField.setText("1")

        // Firestore에서 문서 확인 및 초기값 설정
        db.collection("users").document(userId).collection("visitData").document(restaurantName).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val totalSpent = document.getDouble("totalSpent") ?: 0.0
                    val visitCount = document.getLong("visitCount")?.toInt() ?: 0
                    totalSpentField.setText(totalSpent.toString())
                    visitCountField.setText((visitCount + 1).toString())
                }
            }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        submitButton.setOnClickListener {
            val menu = menuField.text.toString()
            val price = priceField.text.toString().toDoubleOrNull() ?: 0.0
            val rating = ratingBar.rating.toInt()
            val visitCount = visitCountField.text.toString().toIntOrNull() ?: 1
            val totalSpent = (totalSpentField.text.toString().toDoubleOrNull() ?: 0.0) + price

            // visitData에 위도와 경도 저장
            val visitData = mapOf(
                "restaurantName" to restaurantName,
                "menu" to menu,
                "amountSpent" to price,
                "totalSpent" to totalSpent,
                "rating" to rating,
                "visitCount" to visitCount,
                "lastVisitDate" to currentDate,
                "latitude" to latLng.latitude,
                "longitude" to latLng.longitude
            )

            db.collection("users").document(userId).collection("visitData").document(restaurantName)
                .set(visitData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Error saving visit data", e)
                    Toast.makeText(requireContext(), "저장 실패", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun displayFilteredMarkers(ratingFilter: Int?) {
        clearAllMarkers()
        Log.d("Filtering","called!!!!!")

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId).collection("visitData").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val restaurantName = document.getString("restaurantName") ?: continue
                    val rating = document.getLong("rating")?.toInt() ?: continue
                    val totalSpent = document.getDouble("totalSpent") ?: continue

                    if (ratingFilter == null || rating == ratingFilter) {
                        val latLng = LatLng(document.getDouble("latitude") ?: 0.0, document.getDouble("longitude") ?: 0.0)
                        val marker = Marker()
                        marker.position = latLng
                        marker.captionText = restaurantName
                        marker.map = naverMap

                        when (rating) {
                            3 -> marker.iconTintColor = resources.getColor(R.color.gold)
                            2 -> marker.iconTintColor = resources.getColor(R.color.silver)
                            1 -> marker.iconTintColor = resources.getColor(R.color.bronze)
                        }

                        markers.add(marker)

                        for (marker in markers) {
                            marker.setOnClickListener {
                                showRestaurantInfoDialog(marker.captionText ?: "", latLng)
                                true
                            }
                        }
                    }
                }

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "식당 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupFilterSpinner() {
        val filterSpinner = view?.findViewById<Spinner>(R.id.filter_spinner)
        filterSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> displayFilteredMarkers(null) // 모든 식당
                    1 -> displayFilteredMarkers(3)   // 미슐랭 3스타
                    2 -> displayFilteredMarkers(2)   // 미슐랭 2스타
                    3 -> displayFilteredMarkers(1)   // 미슐랭 1스타
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun navigateToTableSelection(restaurantName: String, latLng: LatLng) {
        val db = FirebaseFirestore.getInstance()

        db.collection("restaurants")
            .whereEqualTo("name", restaurantName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val restaurant = documents.documents[0]
                    val restaurantId = restaurant.id

                    findNavController().navigate(
                        R.id.action_menu_map_to_booking,
                        bundleOf(
                            "restaurantId" to restaurantId,
                            "restaurantName" to restaurantName
                        )
                    )
                } else {
                    // 레스토랑을 찾지 못한 경우 예약 불가 메시지 표시
                    Toast.makeText(
                        context,
                        "현재 예약이 불가능한 레스토랑입니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "레스토랑 정보 조회 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearAllMarkers() {
        markers.forEach { it.map = null }
        markers.clear()
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // 한국어 설정
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(requireContext(), "음성인식을 시작합니다...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Toast.makeText(requireContext(), "음성 인식 오류: $error", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val query = matches[0]
                    searchBar.setText(query)
                    searchRestaurants(query)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startVoiceRecognition() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissions.contains(Manifest.permission.RECORD_AUDIO) &&
                grantResults[permissions.indexOf(Manifest.permission.RECORD_AUDIO)] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(requireContext(), "음성 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "음성 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    data class Restaurants(val name: String, val roadAddress: String)
    private fun LatLng.distanceTo(target: LatLng): Double {
        val latDiff = this.latitude - target.latitude
        val lngDiff = this.longitude - target.longitude
        return sqrt(latDiff.pow(2) + lngDiff.pow(2)) // Euclidean distance (approximation)
    }
}
