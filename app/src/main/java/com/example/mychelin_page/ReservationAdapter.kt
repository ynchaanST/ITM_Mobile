//package com.example.mychelin_page
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.mychelin_page.databinding.ItemReservationBinding
//
///**
// * ReservationAdapter: 예약 데이터를 RecyclerView에 표시하기 위한 어댑터
// * @param visitDataList 표시할 예약 데이터 리스트
// */
//class ReservationAdapter(private val visitDataList: List<VisitData>) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {
//
//    /**
//     * ViewHolder를 생성하는 메서드
//     * @param parent 부모 뷰그룹 (RecyclerView)
//     * @param viewType 뷰 타입 (현재는 단일 타입만 사용)
//     * @return 생성된 ViewHolder 객체
//     */
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
//        // XML 레이아웃을 바인딩 객체로 변환하여 ViewHolder에 전달
//        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ReservationViewHolder(binding)
//    }
//
//    /**
//     * 데이터와 ViewHolder를 바인딩하는 메서드
//     * @param holder 현재 위치의 ViewHolder
//     * @param position 리스트에서의 아이템 위치
//     */
//    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
//        holder.bind(visitDataList[position]) // 데이터 바인딩
//    }
//
//    /**
//     * 데이터 리스트의 크기를 반환하는 메서드
//     * @return 데이터 리스트의 크기
//     */
//    override fun getItemCount(): Int = visitDataList.size
//
//    /**
//     * ReservationViewHolder: 각 리스트 아이템을 표현하는 ViewHolder 클래스
//     * @param binding 아이템 레이아웃의 바인딩 객체
//     */
//    class ReservationViewHolder(private val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root) {
//        /**
//         * 데이터를 아이템 뷰에 바인딩하는 메서드
//         * @param data 바인딩할 VisitData 객체
//         */
//        fun bind(data: VisitData) {
//            binding.restaurantNameTextView.text = data.restaurantName // 식당 이름 표시
//            binding.reservationTimeTextView.text = data.reservationTime // 예약 시간 표시
//        }
//    }
//}
