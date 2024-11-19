package com.example.mychelin_page

/**
 * VisitData: 방문 예약 정보를 저장하는 데이터 클래스
 * Firebase 데이터베이스와 매핑됩니다.
 * @property id 예약의 고유 ID
 * @property restaurantName 예약된 식당 이름
 * @property reservationTime 예약 시간
 */
data class VisitData(
    val id: String = "", // 고유 ID (기본값: 빈 문자열)
    val restaurantName: String = "", // 식당 이름
    val reservationTime: String = "" // 예약 시간
)
