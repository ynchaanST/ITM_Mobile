//package com.example.mychelin_page
//
//import androidx.fragment.app.Fragment
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.mychelin_page.databinding.FragmentReservationBinding
//import com.google.firebase.database.*
//
///**
// * ReservationFragment: 예약 정보를 보여주는 프래그먼트
// * Firebase에서 데이터를 가져와 RecyclerView에 표시합니다.
// */
//class ReservationFragment : Fragment() {
//    // 뷰 바인딩 객체: Fragment의 레이아웃 요소에 쉽게 접근하기 위해 사용
//    private lateinit var binding: FragmentReservationBinding
//
//    // 방문 데이터 리스트: Firebase에서 가져온 예약 데이터를 저장
//    private val visitDataList = mutableListOf<VisitData>()
//
//    // Firebase 데이터베이스 참조 객체
//    private lateinit var database: DatabaseReference
//
//    /**
//     * Fragment의 UI를 생성하는 메서드
//     * @param inflater 레이아웃 XML을 뷰 객체로 변환하는 데 사용
//     * @param container 부모 뷰그룹 (Fragment가 포함될 뷰)
//     * @param savedInstanceState 이전 상태를 복원하는 데 사용
//     * @return 생성된 뷰 객체를 반환
//     */
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        // FragmentReservationBinding을 사용하여 XML 레이아웃을 연결
//        binding = FragmentReservationBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    /**
//     * 뷰가 생성된 후 호출되는 메서드
//     * 여기서 데이터 초기화 및 RecyclerView 설정을 처리합니다.
//     */
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        initializeFirebase() // Firebase 초기화
//        setupRecyclerView()  // RecyclerView 설정
//        fetchDataFromServer() // 서버에서 데이터 가져오기
//    }
//
//    /**
//     * Firebase 초기화 메서드
//     * Firebase 데이터베이스 참조 객체를 초기화합니다.
//     */
//    private fun initializeFirebase() {
//        // Firebase 실시간 데이터베이스의 URL을 설정하고 참조 객체를 초기화
//        database = FirebaseDatabase.getInstance("https://mychelin-eb6c9.firebaseio.com").reference
//    }
//
//    /**
//     * RecyclerView 설정 메서드
//     * 어댑터와 레이아웃 매니저를 설정하여 RecyclerView를 준비합니다.
//     */
//    private fun setupRecyclerView() {
//        // ReservationAdapter를 생성하여 RecyclerView에 연결
//        val adapter = ReservationAdapter(visitDataList)
//        binding.reservationRecyclerView.apply {
//            this.adapter = adapter // 어댑터 설정
//            layoutManager = LinearLayoutManager(requireContext()) // 레이아웃 매니저 설정 (세로 방향 리스트)
//        }
//    }
//
//    /**
//     * 서버에서 방문 데이터를 가져오는 메서드
//     * Firebase에서 데이터를 비동기로 읽어와 리스트에 추가합니다.
//     */
//    private fun fetchDataFromServer() {
//        // Firebase 데이터베이스의 "visit_data" 노드에 이벤트 리스너 추가
//        database.child("visit_data").addValueEventListener(object : ValueEventListener {
//            /**
//             * 데이터가 변경되었을 때 호출되는 콜백 메서드
//             * @param snapshot 현재 데이터베이스 상태를 포함한 스냅샷
//             */
//            override fun onDataChange(snapshot: DataSnapshot) {
//                // 기존 데이터를 모두 지우고 새로 가져온 데이터를 추가
//                visitDataList.clear()
//                for (dataSnapshot in snapshot.children) {
//                    // DataSnapshot을 VisitData 객체로 변환
//                    val visitData = dataSnapshot.getValue(VisitData::class.java)
//                    if (visitData != null) {
//                        visitDataList.add(visitData) // 리스트에 추가
//                    }
//                }
//                // 데이터가 변경되었음을 어댑터에 알림 (화면 갱신)
//                binding.reservationRecyclerView.adapter?.notifyDataSetChanged()
//            }
//
//            /**
//             * 데이터 읽기에 실패했을 때 호출되는 콜백 메서드
//             * @param error 발생한 데이터베이스 에러
//             */
//            override fun onCancelled(error: DatabaseError) {
//                // 에러를 처리 (예: 로그 출력 또는 사용자에게 알림)
//            }
//        })
//    }
//}
