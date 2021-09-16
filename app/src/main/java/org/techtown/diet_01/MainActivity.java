package org.techtown.diet_01;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    SimpleDateFormat dd = new SimpleDateFormat("dd");
    SimpleDateFormat tt = new SimpleDateFormat("hhmm");
    int meal_b = 0;
    int meal_l;
    int meal_d;
    String diet;
    String dtime;
    String date;
    String next_date;
    String next_diet;
    TextView dateTextView;
    TextView dietTextView;
    TextView dateTextView2;
    // tomorrow
    TextView tomorrowTextView;
    TextView dietTomorrowTextView;
    TextView tomorrowTextView2;

    String url = "https://domi.seoultech.ac.kr/support/food/?foodtype=sung";
    final Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateTextView = (TextView) findViewById(R.id.datetime);
        dateTextView2 = (TextView) findViewById(R.id.datetime2);
        dietTextView = (TextView) findViewById(R.id.dietTextView);
        tomorrowTextView = (TextView) findViewById(R.id.datetime_tomorrow);
        tomorrowTextView2 = (TextView) findViewById(R.id.datetime2_tomorrow);
        dietTomorrowTextView = (TextView) findViewById(R.id.dietTextView_tomorrow);
        //time
//        dateTextView.setText(getTime());
        // 아침 점심 저녁


        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                String data_dtime;
                String data_dtime2;
                String data_diets;
                String data_dd;
                //년도 월 주
                data_dtime = bundle.getString("dtime");
                dateTextView.setText(data_dtime+'차');
                //요일
                data_dtime2 = bundle.getString("dtime2");
                dateTextView2.setText(data_dtime2);
                //식단
                data_diets = bundle.getString("diets");
                dietTextView.setText(data_diets);
                //일 정보
                data_dd = bundle.getString("dates");

                String tomorrow_dtime;
                String tomorrow_dtime2;
                String tomorrow_diets;
                String tomorrow_dd;
                //년도 월 주
                tomorrow_dtime = bundle.getString("dtime");
                tomorrowTextView.setText(tomorrow_dtime+'차');
                //요일
                tomorrow_dtime2 = bundle.getString("tomorrow_dtime2");
                tomorrowTextView2.setText(tomorrow_dtime2);
                //식단
                tomorrow_diets = bundle.getString("tomorrow_diets");
                dietTomorrowTextView.setText(tomorrow_diets);
                //일 정보
                tomorrow_dd = bundle.getString("tomorrow_dates");
            }
        };

        //crawling
        new Thread(){
            @Override
            public void run() {
                Document doc = null;
                try{
                    doc = Jsoup.connect(url).get();

                    // 주차
                    Element d = doc.select(".t4 b").first();
                    dtime = d.text();
                    bundle.putString("dtime", dtime);
                    String dd=  getdd();

                    // 월 / 요일
                    Elements dates = doc.select(".chang th");
                    int i=0;
                    for(i =0; i<=10; i+=2){
                        String k = dates.get(i).text();
                        if (k.length() == 2){
                            k = "0"+k;
                        }
                        if(k.equals(dd+"일")){
                            break;
                        }
                    }
                    date = dates.get(i+1).text();
                    String [] weeks = {"월","화","수","목","금","토","일"};
                    int j = 0;
                    for (j=0; j<7; j++){
                        if (weeks[j].equals(date)){
                            break;
                        }
                    }
                    if (j+1 == 7){
                        j = 0;
                    }
                    next_date = weeks[j+1];
                    bundle.putString("tomorrow_dtime2", next_date+"요일");
                    Message ndate = handler.obtainMessage();
                    ndate.setData(bundle);
                    handler.sendMessage(ndate);

                    bundle.putString("dtime2", date+"요일");
                    Message date = handler.obtainMessage();
                    date.setData(bundle);
                    handler.sendMessage(date);


                    Elements diets = doc.select(".chang td");
                    diet = diets.get(j).text();
                    meal_l = diet.indexOf("점심");
                    String breakfast = diet.substring(5, meal_l);
                    int choice_index = breakfast.indexOf("토스트");
                    String sub1 = breakfast.substring(0, choice_index);
                    String sub2 = breakfast.substring(choice_index);
                    breakfast = sub1+"<선택>\n"+sub2;
                    meal_d = diet.indexOf("저녁");
                    String lunch = diet.substring(meal_l+5, meal_d);
                    String dinner = diet.substring(meal_d+5);
                    String result = "아침\n\n"+breakfast.replace(",","\n")
                            +"\n\n점심\n\n"+lunch.replace(",","\n")
                            +"\n\n저녁\n\n"+dinner.replace(",","\n")+"\n\n\n\n";
                    bundle.putString("diets", result);
                    Message diet = handler.obtainMessage();
                    diet.setData(bundle);
                    handler.sendMessage(diet);

                    //다음날
                    if (j+1 == 7){
                        bundle.putString("tomorrow_diets", "다음주차 식단 데이터를 가져올 수 없습니다.");
                        Message ndiet = handler.obtainMessage();
                        ndiet.setData(bundle);
                        handler.sendMessage(ndiet);
                    }
                    else {
                        next_diet = diets.get(j + 1).text();
                        meal_l = next_diet.indexOf("점심");
                        String nbreakfast = next_diet.substring(5, meal_l);
                        choice_index = nbreakfast.indexOf("토스트");
                        sub1 = nbreakfast.substring(0, choice_index);
                        sub2 = nbreakfast.substring(choice_index);
                        nbreakfast = sub1 + "<선택>\n" + sub2;
                        meal_d = next_diet.indexOf("저녁");
                        String nlunch = next_diet.substring(meal_l + 5, meal_d);
                        String ndinner = next_diet.substring(meal_d + 5);
                        String nresult = "아침\n\n" + nbreakfast.replace(",", "\n")
                                + "\n\n점심\n\n" + nlunch.replace(",", "\n")
                                + "\n\n저녁\n\n" + ndinner.replace(",", "\n") + "\n\n\n\n";
                        bundle.putString("tomorrow_diets", nresult);
                        Message ndiet = handler.obtainMessage();
                        ndiet.setData(bundle);
                        handler.sendMessage(ndiet);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }


    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    private String getdd(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return dd.format(mDate);
    }

}