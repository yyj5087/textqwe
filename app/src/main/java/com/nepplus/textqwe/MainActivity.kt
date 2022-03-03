package com.nepplus.textqwe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    val mWinNumList = ArrayList<Int>()
    val mWinNumberTextViewList = ArrayList<TextView>()
    val pickNumSet = hashSetOf<Int>()
    var bonusNum = 0

    //    내 번호 6개
//    코틀린은 단순 배열 초기화 int[] arr = {}; 문법 지원 x
//      숫자 목록을 파라미터로 넣으면 > Array로 만들어 주는 함수 설정
    var mMyNumbers = arrayOf(15, 25, 34, 37, 39, 45)

    //    사용한 금액, 당첨된 금액 합산 변수
    var mUseMoney = 0
    var mEarnMoney = 0L //30억 이상의 당첨 대비. Long 타입으로 설정

    //    각 등수별 횟수 카운팅 변수
    var rankCount1 = 0
    var rankCount2 = 0
    var rankCount3 = 0
    var rankCount4 = 0
    var rankCount5 = 0
    var rankCountFail = 0
//    현재 자동 구매가 진행중인지 구별하는 변수
    var isAutoNow = false

//     Handler로 쓰레드에 할일 할당 (postDelayed - 일정 시간 지난 뒤에 할일 할당)

    lateinit var mHandler: Handler

    //    핸들러가 반복 실행할 코드를(로또 다시 구매), 인터페이스를 이용해 변수로 저장
    val buyLottoRunnable = object : Runnable {
        override fun run() {
//        물려받은 추상메쏘드 구현
//        할 일이 어떤건지 적는 함수

//        쓴 돈이 1천만원이 안된다면 추가 구매
            if (mUseMoney <= 10000000) {
                buyLottoActivity()

//            핸들러에게 다음 할일로, 이 코드를 다시 등록
                mHandler.post(this)
            }
//        그렇지 않다면, 할일 정지
            else {
                Toast.makeText(this@MainActivity, "자동 구매가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            }

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupEvents()
        setupValues()

    }

    private fun setupEvents() {

        btnfree.setOnClickListener {
//                처음 눌림면 >반복 구매시작 > 1천만원 사용할떄까지 반복
//                1회 로또 구매 명령> 완료 되면 다시 1회 로또 구매 > .. 연속 클릭을 자동으로 하는 느낌
            if (!isAutoNow){
                mHandler.post(buyLottoRunnable)
//                    자동이 돌고 있다는 표식
                isAutoNow = true
                btnfree.text = "자동 구매 중단하기"
            }
            else{
//                    핸들러에 등록된 다음 할 일(구매) 제거
                mHandler.removeCallbacks(buyLottoRunnable)
                isAutoNow = false
                btnfree.text = "자동 구매 재개하기"
            }
//                핸들러에게, 할일로 처음 등록 (할일 시작)




        }





        btnResult.setOnClickListener {

            buyLottoActivity()
            mWinNumberTextViewList.forEach {
                it.isVisible = true

            }
        }
    }

    private fun buyLottoActivity() {

        mUseMoney += 1000


        mWinNumList.clear()


        for (i in 0..5) {
            while (true) {
                val randomNum = (Math.random() * 45 + 1).toInt()
                if (!mWinNumList.contains(randomNum)) {
                    mWinNumList.add(randomNum)
                    break
                }

            }
        }
        mWinNumList.sort()
        Log.d("당첨 번호", mWinNumList.toString())

        mWinNumList.forEachIndexed { index, winNum ->
            mWinNumberTextViewList[index].text = winNum.toString()
        }
        while (true) {
            val randomNum = (Math.random() * 45 + 1).toInt()
            if (!mWinNumList.contains(randomNum)) {
                bonusNum = randomNum
                break
            }
        }
        txtBouNum.text = bonusNum.toString()
//        내 숫자 6개와 비교, 등수 판정
        checkLottoRank()



        clearButton.setOnClickListener {
            mWinNumList.clear()
            mWinNumberTextViewList.forEach {
                it.isVisible = false
            }
        }
    }

    private fun checkLottoRank() {

//        내 번호 목록 / 당첨 번호 목록중, 같은 숫자가 몇개?
        var correctCount = 0

//        내 번호를 하나씩 조회
        for (myNum in mMyNumbers) {
//            당첨번호를 맞췄는가? => 당첨번호 목록에 내번호가 들어있나?
            if (mWinNumList.contains(myNum)) {
                correctCount++
            }
        }
//      맞춘 개수에 따른 등수 판정

        when (correctCount) {
            6 -> {
//                30억을 번 금액으로 추가
                mEarnMoney += 3000000000
//                1등 횟수
                rankCount1++
            }
            5 -> {
//                보너스 번호를 맞췄는지? => 보너스번호가 내 번호 목록에 들어있나?
                if (mMyNumbers.contains(bonusNum)) {
                    mEarnMoney += 50000000
                    rankCount2++
                } else {
                    mEarnMoney += 2000000
                    rankCount3++
                }
            }
            4 -> {
                mEarnMoney += 50000
                rankCount4++
            }
            3 -> {
//                5등 -> 5천원 사용한 돈을 줄여주자
                mUseMoney -= 5000
                rankCount5++
            }
            else -> {
                rankCountFail++
            }

        }
//        사용 금액 / 당첨 금액을 텍스트뷰에 각각 반영
        txtUsedMoney.text = "${NumberFormat.getInstance().format(mUseMoney)} 원"
        txtEarnMoney.text = "${NumberFormat.getInstance().format(mEarnMoney)} 원"
//        등수별로 횟수도 텍스트뷰에 반영
        txtRankCount1.text = "${rankCount1}"
        txtRankCount2.text = "${rankCount2}"
        txtRankCount3.text = "${rankCount3}"
        txtRankCount4.text = "${rankCount4}"
        txtRankCount5.text = "${rankCount5}"
        txtRankCount6.text = "${rankCountFail}"
    }

    private fun setupValues() {

//        반복을 담당할 핸들러를 생성
        mHandler = Handler(Looper.getMainLooper())

        mWinNumberTextViewList.add(txtWinNum01)
        mWinNumberTextViewList.add(txtWinNum02)
        mWinNumberTextViewList.add(txtWinNum03)
        mWinNumberTextViewList.add(txtWinNum04)
        mWinNumberTextViewList.add(txtWinNum05)
        mWinNumberTextViewList.add(txtWinNum06)


    }

}