package com.example.mychelin_page

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource

class SearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    private lateinit var locationSource: FusedLocationSource

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)

        // MapView 초기화
        mapView = rootView.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return rootView
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        // 위치 소스 초기화
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // 위치 권한 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 위치 변경 리스너 설정
        naverMap.addOnLocationChangeListener { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            Log.d("SearchFragment", "현재 위치: $latitude, $longitude")

            // 주변 식당 로드 (필요에 따라 구현)
             loadNearbyRestaurants(latitude, longitude)
        }

        // 미슐랭 스타 식당 표시
        displayMichelinStarredRestaurants()
    }

    private fun displayMichelinStarredRestaurants() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userVisitsRef = db.collection("users").document(currentUser.uid).collection("visits")

            // michelinStars 필드가 0보다 큰 데이터 가져오기
            userVisitsRef.whereGreaterThan("michelinStars", 0)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val visitData = document.toObject(VisitData::class.java)

                        // 마커 생성
                        val marker = Marker()
                        marker.position = LatLng(visitData.latitude, visitData.longitude)
                        marker.captionText = visitData.restaurantName

                        // 커스텀 아이콘 설정 (미슐랭 스타 이미지)
                        val michelinStarIcon = OverlayImage.fromResource(R.drawable.ic_michelin_star)
                        marker.icon = michelinStarIcon

                        marker.map = naverMap
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SearchFragment", "미슐랭 스타 식당 데이터 불러오기 오류", e)
                }
        } else {
            Toast.makeText(requireContext(), "사용자가 로그인되어 있지 않습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 기존 코드 (loadNearbyRestaurants, showRestaurantDetails, saveVisitData 등)
    private fun loadNearbyRestaurants(latitude: Double, longitude: Double) {
        // Fetch nearby restaurants using Naver Places API or any other source
        // For demonstration, let's use dummy data

        val restaurants = listOf(
            Restaurant("Restaurant A", latitude + 0.001, longitude + 0.001),
            Restaurant("Restaurant B", latitude - 0.001, longitude - 0.001)
        )

        for (restaurant in restaurants) {
            val marker = Marker()
            marker.position = LatLng(restaurant.latitude, restaurant.longitude)
            marker.map = naverMap
            marker.captionText = restaurant.name

            // Set a tag to the marker
            marker.tag = restaurant

            marker.setOnClickListener { overlay ->
                val selectedRestaurant = overlay.tag as Restaurant
                // Show restaurant details and allow user to save visit data
                showRestaurantDetails(selectedRestaurant)
                true
            }
        }
    }

    private fun showRestaurantDetails(restaurant: Restaurant) {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_restaurant_details, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        // Initialize views in the dialog
        val menuEditText = dialogView.findViewById<EditText>(R.id.menu_edit_text)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amount_edit_text)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.rating_bar)
        val michelinStarsEditText = dialogView.findViewById<EditText>(R.id.michelin_stars_edit_text)
        val visitCountEditText = dialogView.findViewById<EditText>(R.id.visit_count_edit_text)
        val photoButton = dialogView.findViewById<Button>(R.id.photo_button)
        val photoImageView = dialogView.findViewById<ImageView>(R.id.photo_image_view)

        var selectedPhotoUri: Uri? = null

        // Set up photo selection
        val selectPhotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedPhotoUri = uri
                photoImageView.setImageURI(uri)
            }
        }

        photoButton.setOnClickListener {
            selectPhotoLauncher.launch("image/*")
        }

        builder.setTitle(restaurant.name)
        builder.setPositiveButton("Save") { _, _ ->
            // Get input data
            val menu = menuEditText.text.toString()
            val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
            val rating = ratingBar.rating
            val michelinStars = michelinStarsEditText.text.toString().toIntOrNull() ?: 0
            val visitCount = visitCountEditText.text.toString().toIntOrNull() ?: 1

            // Create VisitData object
            val visitData = VisitData(
                restaurantName = restaurant.name,
                menu = menu,
                amountSpent = amount,
                rating = rating,
                michelinStars = michelinStars,
                visitCount = visitCount,
                photoUri = selectedPhotoUri?.toString() ?: ""
            )

            // Save data to Firestore
            saveVisitData(visitData)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun saveVisitData(visitData: VisitData) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userVisitsRef = db.collection("users").document(currentUser.uid).collection("visits")

            // If photo is selected, upload it to Firebase Storage
            if (visitData.photoUri.isNotEmpty()) {
                val photoUri = Uri.parse(visitData.photoUri)
                val storageRef = FirebaseStorage.getInstance().reference.child("visit_photos/${currentUser.uid}/${System.currentTimeMillis()}.jpg")
                val uploadTask = storageRef.putFile(photoUri)

                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        visitData.photoUri = uri.toString()
                        // Save visit data with photo URL
                        userVisitsRef.add(visitData)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(requireContext(), "Visit data saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("SearchFragment", "Error saving visit data", e)
                            }
                    }
                }.addOnFailureListener { e ->
                    Log.e("SearchFragment", "Photo upload failed", e)
                    // Save visit data without photo
                    userVisitsRef.add(visitData)
                }
            } else {
                // Save visit data without photo
                userVisitsRef.add(visitData)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(requireContext(), "Visit data saved", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("SearchFragment", "Error saving visit data", e)
                    }
            }
        } else {
            Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }
    // Handle location permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    // Lifecycle methods
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

    // 데이터 클래스
    data class Restaurant(val name: String, val latitude: Double, val longitude: Double)

    data class VisitData(
        val restaurantName: String = "",
        val menu: String = "",
        val amountSpent: Double = 0.0,
        val rating: Float = 0f,
        val michelinStars: Int = 0,
        val visitCount: Int = 0,
        var photoUri: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    )
}
