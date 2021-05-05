package com.coolweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coolweather.gson.Forecast;
import com.coolweather.gson.Weather;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView mWeatherLayout;
    private TextView mTitleCity;
    private TextView mTitleUpdateTime;
    private TextView mWeatherInfoText;
    private TextView mDegreeText;
    private LinearLayout mForecastLayout;
    private TextView mAqiText;
    private TextView mPm25Text;
    private TextView mComfortText;
    private TextView mCarWashText;
    private TextView mSportText;
    private SharedPreferences mSp;
    private String mWeatherString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initView();
    }

    private void initView() {
        mWeatherLayout = this.findViewById(R.id.weather_layouts);
        mTitleCity = this.findViewById(R.id.title_city);
        mTitleUpdateTime = this.findViewById(R.id.title_update_times);
        mWeatherInfoText = this.findViewById(R.id.weather_info_texts);
        mDegreeText = this.findViewById(R.id.degree_texts);
        mForecastLayout = this.findViewById(R.id.forecast_layouts);
        mAqiText = this.findViewById(R.id.aqi_text);
        mPm25Text = this.findViewById(R.id.pm25_text);
        mComfortText = this.findViewById(R.id.comfort_texts);
        mCarWashText = this.findViewById(R.id.car_wash_texts);
        mSportText = this.findViewById(R.id.sport_texts);
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        mWeatherString = mSp.getString("weather",null);
        if (mWeatherString != null) {
            Weather weather = Utility.handleWeatherResponse(mWeatherString);
            showWeatherInfo(weather);
        }else {
            String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    //根据天气id请求城市天气信息
    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                "&key=0be65a5e5e3a488fac51a9f0bba7dd44";
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor =
                                    PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.mBasic.cityName;
        String updateTiem = weather.mBasic.mUpdate.updateTime.split(" ")[1];
        String degree = weather.mNow.temperature + "℃";
        String weatherInfo = weather.mNow.more.info;
        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTiem);
        mDegreeText.setText(degree);
        mWeatherInfoText.setText(weatherInfo);
        mForecastLayout.removeAllViews();

        for (Forecast forecast : weather.mForecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, mForecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_texts);
            TextView infoText = view.findViewById(R.id.info_texts);
            TextView maxText = view.findViewById(R.id.max_texts);
            TextView minText = view.findViewById(R.id.min_texts);
            dateText.setText(forecast.date);
            infoText.setText(forecast.mMore.info);
            maxText.setText(forecast.mTemperature.max);
            minText.setText(forecast.mTemperature.min);
            mForecastLayout.addView(view);
        }
        if (weather.mAQI != null) {
            mAqiText.setText(weather.mAQI.mCity.aqi);
            mPm25Text.setText(weather.mAQI.mCity.pm25);
        }
        String comfort = "舒适度：" + weather.mSuggestion.comfort.info;
        String carWash = "洗车指数：" + weather.mSuggestion.carWash.info;
        String sport = "运动建议：" + weather.mSuggestion.sport.info;
        mComfortText.setText(comfort);
        mCarWashText.setText(carWash);
        mSportText.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);
    }
}
