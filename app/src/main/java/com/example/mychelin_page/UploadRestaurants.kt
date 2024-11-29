package com.example.mychelin_page

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem
import org.locationtech.proj4j.ProjCoordinate
import org.locationtech.proj4j.CoordinateTransformFactory
import java.io.BufferedReader
import java.io.InputStreamReader

class UploadRestaurants : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firestore 초기화
        val db = FirebaseFirestore.getInstance()

        // JSON 파일 읽기
        val inputStream = resources.openRawResource(R.raw.restaurants)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = reader.use { it.readText() } // JSON 파일 내용을 문자열로 읽음

        // JSON 파싱
        val jsonArray = JSONArray(jsonString)

        // 좌표계 정의
        val crsFactory = CRSFactory()
        val sourceCRS: CoordinateReferenceSystem = crsFactory.createFromName("EPSG:2097") // 중부원점TM
        val targetCRS: CoordinateReferenceSystem = crsFactory.createFromName("EPSG:4326") // WGS84
        val transformFactory = CoordinateTransformFactory()
        val transform = transformFactory.createTransform(sourceCRS, targetCRS)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            // 필요한 데이터 추출
            val statusName = jsonObject.optString("상세영업상태명")
            val name = jsonObject.optString("사업장명")
            val tmX = jsonObject.optDouble("좌표정보(X)", Double.NaN)
            val tmY = jsonObject.optDouble("좌표정보(Y)", Double.NaN)

            // 상세영업상태명이 "영업"이고 좌표가 존재하는 경우만 Firestore에 업로드
            if (statusName == "영업" && !tmX.isNaN() && !tmY.isNaN()) {
                // 중부원점TM -> WGS84로 변환
                val sourceCoord = ProjCoordinate(tmX, tmY)
                val targetCoord = ProjCoordinate()
                transform.transform(sourceCoord, targetCoord)

                // Firestore에 저장할 데이터
                val restaurantId = "${name}_${targetCoord.y}_${targetCoord.x}" // 고유 ID 생성
                val restaurant = mapOf(
                    "name" to name,
                    "latitude" to targetCoord.y,
                    "longitude" to targetCoord.x
                )

                // Firestore에 추가
                db.collection("restaurants").document(restaurantId).set(restaurant)
                    .addOnSuccessListener {
                        Log.d("FirestoreUpload", "Success: $name")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreUpload", "Error uploading $name", e)
                    }
            }
        }

        Log.d("JsonToFirestore", "JSON processing complete.")
    }
}
