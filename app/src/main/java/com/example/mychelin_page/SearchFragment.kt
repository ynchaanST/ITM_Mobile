package com.example.mychelin_page

import VisitData
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        // 위치 권한 확인 및 설정
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // 위치 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 지도 클릭 리스너 설정
        naverMap.setOnMapClickListener { pointF, latLng ->
            addMichelinMarker(latLng)
        }

        // 미슐랭 스타 식당 표시
        displayMichelinStarredRestaurants()
    }

    private fun createScaledOverlayImage(resourceId: Int, width: Int, height: Int): OverlayImage {
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        return OverlayImage.fromBitmap(scaledBitmap)
    }

    private fun addMichelinMarker(latLng: LatLng) {
        val marker = Marker()
        marker.position = latLng
        marker.icon = createScaledOverlayImage(R.drawable.ic_michelin_star, 50, 50)  // 미슐랭 스타 아이콘 설정
        marker.map = naverMap

        // 마커 클릭 리스너 설정
        marker.setOnClickListener { overlay ->
            showVisitData(null, latLng)
            true
        }
    }

    private fun showVisitData(visitData: VisitData?, latLng: LatLng) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_restaurant_details, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        // 뷰 초기화
        val restaurantNameEditText =
            dialogView.findViewById<EditText>(R.id.restaurant_name_edit_text)
        val menuEditText = dialogView.findViewById<EditText>(R.id.menu_edit_text)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amount_edit_text)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.rating_bar)
        val michelinStarsEditText = dialogView.findViewById<EditText>(R.id.michelin_stars_edit_text)
        val visitCountEditText = dialogView.findViewById<EditText>(R.id.visit_count_edit_text)

        if (visitData != null) {
            // 기존 데이터 설정
            restaurantNameEditText.setText(visitData.restaurantName)
            menuEditText.setText(visitData.menu)
            amountEditText.setText(visitData.amountSpent.toString())
            ratingBar.rating = visitData.rating
            michelinStarsEditText.setText(visitData.michelinStars.toString())
            visitCountEditText.setText(visitData.visitCount.toString())
        }

        builder.setPositiveButton("저장") { _, _ ->
            // 입력 데이터 가져오기
            val restaurantName = restaurantNameEditText.text.toString()
            val menu = menuEditText.text.toString()
            val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
            val rating = ratingBar.rating
            val michelinStars = michelinStarsEditText.text.toString().toIntOrNull() ?: 0
            val visitCount = visitCountEditText.text.toString().toIntOrNull() ?: 1

            val restaurantId = generateRestaurantId(restaurantName, latLng)

            // VisitData 객체 생성
            val newVisitData = VisitData(
                restaurantId = restaurantId,
                restaurantName = restaurantName,
                menu = menu,
                amountSpent = amount,
                rating = rating,
                michelinStars = michelinStars,
                visitCount = visitCount,
                latitude = latLng.latitude,
                longitude = latLng.longitude
            )

            // 데이터 저장
            saveVisitData(newVisitData)
        }
        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun saveVisitData(visitData: VisitData) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userVisitsRef =
                db.collection("users").document(currentUser.uid).collection("visits")
            val visitDataRef = userVisitsRef.document(visitData.restaurantId)

            // VisitData 저장
            visitDataRef.set(visitData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "방문 데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    // 마커 갱신을 위해 지도 다시 로드
                    displayMichelinStarredRestaurants()
                }
                .addOnFailureListener { e ->
                    Log.e("SearchFragment", "Error saving visit data", e)
                }
        } else {
            Toast.makeText(requireContext(), "사용자가 로그인되어 있지 않습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private val markers = mutableListOf<Marker>()

    private fun displayMichelinStarredRestaurants() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userVisitsRef = db.collection("users").document(currentUser.uid).collection("visits")

            userVisitsRef.get()
                .addOnSuccessListener { documents ->
                    clearAllMarkers() // 기존 마커 제거

                    for (document in documents) {
                        val visitData = document.toObject(VisitData::class.java)

                        val marker = Marker()
                        marker.position = LatLng(visitData.latitude, visitData.longitude)
                        marker.captionText = visitData.restaurantName
                        marker.icon = createScaledOverlayImage(R.drawable.ic_michelin_star, 32, 32)
                        marker.map = naverMap


                        markers.add(marker)

                        marker.setOnClickListener { overlay ->
                            showVisitData(
                                visitData,
                                LatLng(visitData.latitude, visitData.longitude)
                            )
                            true
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SearchFragment", "방문 데이터 불러오기 오류", e)
                }
        } else {
            Toast.makeText(requireContext(), "사용자가 로그인되어 있지 않습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearAllMarkers() {
        for (marker in markers) {
            marker.map = null
        }
        markers.clear()
    }

    private fun generateRestaurantId(restaurantName: String, latLng: LatLng): String {
        return "${restaurantName}_${latLng.latitude}_${latLng.longitude}"
    }

    // 위치 권한 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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
}
