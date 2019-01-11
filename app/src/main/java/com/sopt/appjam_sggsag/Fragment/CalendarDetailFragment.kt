package com.sopt.appjam_sggsag.Fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.util.EventLog
import android.util.JsonReader
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.baoyz.swipemenulistview.SwipeMenu
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import com.sopt.appjam_sggsag.Adapter.CalendarRecyclerAdapter
import com.sopt.appjam_sggsag.Adapter.CalendarRecyclerAdapter2
import com.sopt.appjam_sggsag.Data.CalendarDateData
import com.sopt.appjam_sggsag.Data.EventList
import com.sopt.appjam_sggsag.Interface.GetYearMonthTab
import com.sopt.appjam_sggsag.MyApplication

import com.sopt.appjam_sggsag.R
import kotlinx.android.synthetic.main.fragment_calendar_detail.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import com.baoyz.swipemenulistview.SwipeMenuLayout
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sopt.appjam_sggsag.CalendarDetailActivity
import com.sopt.appjam_sggsag.DB.SharedPreferenceController
import com.sopt.appjam_sggsag.Network.NetworkService
import com.sopt.appjam_sggsag.Post.CalendarData
import com.sopt.appjam_sggsag.Post.PostCalendarResponse
import org.jetbrains.anko.support.v4.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

var tempcount = 0

class CalendarDetailFragment : Fragment(), GetYearMonthTab {

    lateinit var recyclerViewAdapter: CalendarRecyclerAdapter
    lateinit var recyclerViewAdapter3: CalendarRecyclerAdapter2


    var mArrayList: ArrayList<EventList> = ArrayList()

    var yyear: Int = 0
    var mmonth: Int = 0
    var dday: String = ""
    var listAdapter: ListDataAdapter = ListDataAdapter()
    var listServer: ArrayList<CalendarData>? = ArrayList()

    val networkService: NetworkService by lazy {
        MyApplication.instance.networkService
    }

    override fun getYearMonthTab(year: String, month: String) {

    }

    override fun onClick(year: Int, month: Int, day: String) {
        mArrayList.clear()
        yyear = year
        mmonth = month
        dday = day
        toast(dday)
        val params =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 689)
        val animation = AlphaAnimation(0f, 1f)
        animation.duration = 400
        frag_calendar_detail_recycle_view.layoutParams = params
        recyclerViewAdapter3 = CalendarRecyclerAdapter2(activity!!, dataList, scheduleList, month, this)
        frag_calendar_detail_recycle_view.adapter = recyclerViewAdapter3
        frag_calendar_detail_recycle_view.layoutManager = GridLayoutManager(getActivity(), 7)
        ll_todo_all_list.visibility = View.VISIBLE
        ll_todo_all_list.setAnimation(animation)
        tv_todo_title2.setText((month + 1).toString() + "월 " + dday + "일")
        //list에 표시할 정보 골라내기
        //Toast.makeText(activity!!, "바보도희"+year.toString(),Toast.LENGTH_SHORT).show()
        //toast("날짜래요:"+dday)
        var count = 0
        for (i in 0..scheduleList.size - 1) {
            if (scheduleList[i].year == yyear && scheduleList[i].month == mmonth + 1 && scheduleList[i].day.toString() == dday) {
                mArrayList.add(EventList(year, mmonth + 1, dday.toInt(), scheduleList[i].eventName, 2))
                //변경사항 카테고리
//                var eee = mArrayList[i].eventName
                count++
            }
        }
        if (count > 4) {
            //표시 추가
        }
        listAdapter.notifyDataSetChanged()
        toast("던져버려" + count.toString())
    }


    var dataList: ArrayList<CalendarDateData> = ArrayList()
    var scheduleList: ArrayList<EventList> = ArrayList()
    var month: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            month = it.getInt("diff")

        }
//        getCalendarResponse()


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_calendar_detail, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setOnClickListener()
        dataList = (activity!!.application as MyApplication).dataList1
        scheduleList = (activity!!.application as MyApplication).eventList1
        setRecycleView()

        listView2.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT)       //왼쪽 모션 추가

        //scheduleList 받아와서 날짜로 검사해서, 그 size로 for문 돌게.
        //1. scheduleList 받아오기.

        /*
        var count = 0
        for(i in 0..scheduleList.size-1){
            if(scheduleList[i].year==yyear &&scheduleList[i].month==mmonth+1 && scheduleList[i].day.toString()==dday ){
                mArrayList.add(scheduleList[i].eventName)
            }
        }
        */

        listView2.adapter = listAdapter

        val creator = SwipeMenuCreator { menu ->
            // Create different menus depending on the view type
            val goodItem = SwipeMenuItem(getActivity())
            // set item background
            goodItem.background = ColorDrawable(Color.rgb(0x30, 0xB1, 0xF5))
            // set item width
            goodItem.width = 200
            // set a icon
            goodItem.setIcon(R.drawable.ic_task_complete)
            // add to menu
            menu.addMenuItem(goodItem)

            // create "delete" item
            val deleteItem = SwipeMenuItem(getActivity())
            // set item background
            deleteItem.background = ColorDrawable(Color.rgb(0xE4, 0xE4, 0xE4))
            // set item width
            deleteItem.width = 200
            // set a icon
            deleteItem.setIcon(R.drawable.ic_task_delete)
            // add to menu
            menu.addMenuItem(deleteItem)
        }


        listView2.setMenuCreator(creator)



        listView2.setOnItemClickListener { parent, view, position, id ->
            startActivity<CalendarDetailActivity>()
        }

        listView2.setOnMenuItemClickListener(
            object : SwipeMenuListView.OnMenuItemClickListener {
                override fun onMenuItemClick(position: Int, menu: SwipeMenu, index: Int): Boolean {
                    when (index) {
                        0 -> {
                            Toast.makeText(activity!!, position.toString() + "지원완료", Toast.LENGTH_SHORT).show()
                            val view = listView2 as SwipeMenuLayout
                            menu.getMenuItem(0).background =
                                    ColorDrawable(Color.rgb(0xE4, 0xE4, 0xE4))// .setIcon(R.drawable.ic_task_delete)
                            // listView2.mTitle
                            //menu.getMenuItem(0).icon = R.drawable.ic_task_delete
                            //listAdapter.notifyDataSetChanged()
                            /*
                            goodItem.background = ColorDrawable(
                                Color.rgb(0x30, 0xB1,0xF5)
                            )
                            */
                        }
                        1 -> {
                            mArrayList!!.removeAt(position)
                            //서버에서 일정 삭제 요청.
                            listAdapter.notifyDataSetChanged()      //바뀌었다고 알려줌.
                            Toast.makeText(activity!!, "Item deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return true
                }
            })

        listView2.setOnMenuStateChangeListener(object : SwipeMenuListView.OnMenuStateChangeListener {
            override fun onMenuOpen(position: Int) {
                //   toast("onMenuOpen")
            }

            override fun onMenuClose(position: Int) {
                //   toast("onMenuClose")
            }
        })

        listView2.setOnSwipeListener(object : SwipeMenuListView.OnSwipeListener {
            override fun onSwipeStart(position: Int) {
                //  toast("onSwipeStart")
            }

            override fun onSwipeEnd(position: Int) {
                // toast("onSwipeEnd")
            }
        })
    }


    //통신해서 스케쥴리스트에 넣어줄 코드 작성
    private fun getCalendarResponse() {
        var jsonObject = JSONObject()
        jsonObject.put("year", "2019")
        jsonObject.put("month", "01")
        jsonObject.put("day", "00")
        val gsonObject = JsonParser().parse(jsonObject.toString()) as JsonObject

        val token = SharedPreferenceController.getAuthorization(this.context!!)
        val postCalendarResponse: Call<PostCalendarResponse> = networkService.postCalendarResponse(
            "application/json",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJEb0lUU09QVCIsInVzZXJfaWR4IjoxfQ.5lCvAqnzYP4-2pFx1KTgLVOxYzBQ6ygZvkx5jKCFM08"
            ,
            gsonObject
        )

        postCalendarResponse.enqueue(object : Callback<PostCalendarResponse> {
            override fun onFailure(call: Call<PostCalendarResponse>, t: Throwable) {
                Log.e("calendar fail", t.toString())
            }

            override fun onResponse(call: Call<PostCalendarResponse>, response: Response<PostCalendarResponse>) {
                if (response.isSuccessful) {
                    toast(response.body()!!.message)
                    response.body()?.status
                    listServer = response.body()?.data
                    for(i in 0..listServer!!.size-1){
                        var startYear = listServer!![i].posterStartDate.substring(0, 4).toInt()
                        var startMonth = listServer!![i].posterStartDate.substring(5, 7).toInt()
                        var startDay = listServer!![i].posterStartDate.substring(8, 10).toInt()
                        var endYear = listServer!![i].posterEndDate.substring(0, 4).toInt()
                        var endMonth = listServer!![i].posterEndDate.substring(5, 7).toInt()
                        var endDay = listServer!![i].posterEndDate.substring(8, 10).toInt()
                        var eventName = listServer!![i].posterName
                        var category = listServer!![i].categoryIdx
                        Log.e("sampleResponse", startDay.toString() + endDay.toString())
                        if (tempcount==i) {

                            if (startYear == endYear && startMonth == endMonth) {
                                for (i in startDay..endDay) {
                                    scheduleList.add(EventList(startYear, startMonth, i, eventName, category))
                                    Log.e("itest", i.toString())
                                }
                            }
                            tempcount++
                        }
                    }



                    /*
                    startYear = listServer!![1].posterStartDate.substring(0,4).toInt()
                    startMonth = listServer!![1].posterStartDate.substring(5,7).toInt()
                    startDay = listServer!![1].posterStartDate.substring(8,10).toInt()
                    endYear = listServer!![1].posterEndDate.substring(0,4).toInt()
                    endMonth = listServer!![1].posterEndDate.substring(5,7).toInt()
                    endDay = listServer!![1].posterEndDate.substring(8,10).toInt()
                    eventName = listServer!![1].posterName
                    category = listServer!![1].categoryIdx
                    Log.e("sampleResponse",startMonth.toString())

                    if(startYear==endYear && startMonth==endMonth){
                        for(i in startDay..endDay){
                            scheduleList.add(EventList(startYear,startMonth,i,eventName,category ))
                            Log.e("itest",i.toString())
                        }
                    }

                    */

                    Log.e("calendar success", response.body()!!.message)
                }
            }
        })
    }

    private fun setRecycleView() {
        //임시데이터

        //리사이클러뷰 어댑터를 만들어서 아래처럼 고대로 하면 돼! 그 리사이클러뷰 어댑터에서 ctx는 activity!! 이거 넘겨주면되고!

        recyclerViewAdapter = CalendarRecyclerAdapter(activity!!, dataList, scheduleList, month, this)
        frag_calendar_detail_recycle_view.adapter = recyclerViewAdapter
        frag_calendar_detail_recycle_view.layoutManager = GridLayoutManager(getActivity(), 7)


    }

    private fun setOnClickListener() {
        iv_big_calendar2.setOnClickListener {
            //달력 커지게 하는 이벤트.
            //대신 중요한 점은 보고있던 달력 그대로 넘어가야 한다는 점.
            val params =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

            val animation2 = AlphaAnimation(0f, 1f)
            animation2.duration = 600
            frag_calendar_detail_recycle_view.layoutParams = params

            ll_todo_all_list.visibility = View.GONE
            ll_todo_all_list.setAnimation(animation2)
            setRecycleView()
        }
    }


    inner class ListDataAdapter : BaseAdapter() {

        var holder: ViewHolder? = null

        override fun getCount(): Int {
            return mArrayList!!.size
        }

        override fun getItem(i: Int): Any? {
            return null
        }

        override fun getItemId(i: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView

            if (convertView == null) {
                holder = ViewHolder()
                convertView = layoutInflater.inflate(R.layout.list_item, null)
                // holder.mTextview
                holder?.mCategoryview = convertView!!.findViewById<View>(R.id.tv_rv_todo_category) as TextView
                holder?.mTitleview = convertView!!.findViewById<View>(R.id.tv_rv_todo_title) as TextView
                holder?.mPeriodview = convertView!!.findViewById<View>(R.id.tv_rv_todo_period) as TextView
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }

            when (mArrayList.get(position).category) {
                0 -> {
                    holder?.mCategoryview!!.text = "공모전"
                    holder?.mCategoryview!!.setTextColor(ContextCompat.getColor(ctx, R.color.color0))
                }
                1 -> {
                    holder?.mCategoryview!!.text = "대외활동"
                    holder?.mCategoryview!!.setTextColor(ContextCompat.getColor(ctx, R.color.color1))
                }
                2 -> {
                    holder?.mCategoryview!!.text = "동아리"
                    holder?.mCategoryview!!.setTextColor(ContextCompat.getColor(ctx, R.color.color2))
                }
                3 -> {
                    holder?.mCategoryview!!.text = "교내활동"
                    holder?.mCategoryview!!.setTextColor(ContextCompat.getColor(ctx, R.color.color3))
                }
                4 -> {
                    holder?.mCategoryview!!.text = "채용"
                    holder?.mCategoryview!!.setTextColor(ContextCompat.getColor(ctx, R.color.color4))
                }
                5 -> {
                    holder?.mCategoryview!!.text = "기타"
                    holder?.mCategoryview!!.setTextColor(ContextCompat.getColor(ctx, R.color.color5))
                }
            }
            holder?.mTitleview!!.text = mArrayList!!.get(position).eventName

            //계산코드
            holder?.mPeriodview!!.text = (mArrayList!!.get(position).month).toString() + "." +
                    mArrayList!!.get(position).day.toString() + " ~ "

            return convertView
        }

        inner class ViewHolder {
            var mCategoryview: TextView? = null
            var mTitleview: TextView? = null
            var mPeriodview: TextView? = null

        }
    }

}
