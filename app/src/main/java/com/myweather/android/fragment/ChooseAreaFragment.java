package com.myweather.android.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myweather.android.MainActivity;
import com.myweather.android.R;
import com.myweather.android.WeatherActivity;
import com.myweather.android.adapter.AreaAdapter;
import com.myweather.android.db.AddCounty;
import com.myweather.android.db.City;
import com.myweather.android.db.County;
import com.myweather.android.db.Province;
import com.myweather.android.util.HttpUtil;
import com.myweather.android.util.RecyclerViewItemDecoration;
import com.myweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseAreaFragment extends Fragment {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private static final int LEVEL_MANAGEMENT = 3;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ImageView locationIvObj;
    private RecyclerView areaRecyclerView;
    private AreaAdapter adapter;
    private List<String> dataList = new ArrayList<>();

    /*
    * 省列表
    * */
    private List<Province> provinceList;

    /*
    * 市列表
    * */
    private List<City> cityList;
    /*
    * 县列表
    * */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selecetedCity;

    /**
     * 当前选中的级别
     */
    private static int currentLevel;

    /**
     * 已添加的县城列表
     */
    private List<AddCounty> addCounties;


    public ChooseAreaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        locationIvObj = (ImageView) view.findViewById(R.id.area_manage_IvId);
        backButton = (Button) view.findViewById(R.id.back_button);
        areaRecyclerView = (RecyclerView) view.findViewById(R.id.area_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        areaRecyclerView.setLayoutManager(layoutManager);
        adapter = new AreaAdapter(getContext(), dataList);
        areaRecyclerView.setAdapter(adapter);
        areaRecyclerView.addItemDecoration(new RecyclerViewItemDecoration(getContext()));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter.setOnItemClickListener(new AreaAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, View v) {
                if (currentLevel == LEVEL_MANAGEMENT && position == 0) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selecetedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    String selectedCountyName = countyList.get(position).getCountyName();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                    addCounties = DataSupport.where("weatherId = ?", weatherId).find(AddCounty.class);
                    if (addCounties.size() <= 0) {
                        AddCounty addCounty = new AddCounty();
                        addCounty.setWeatherId(weatherId);
                        addCounty.setCountyName(selectedCountyName);
                        addCounty.save();
                    }
                    queryAddCounty();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }else if (currentLevel == LEVEL_PROVINCE){
                    queryAddCounty();
                }
            }
        });
        queryAddCounty();
    }

    /**
     * 显示添加的城市
     */
    public void queryAddCounty() {
        titleText.setText("城市管理");
        backButton.setVisibility(View.GONE);
        locationIvObj.setVisibility(View.VISIBLE);
        addCounties = DataSupport.findAll(AddCounty.class);
        dataList.clear();
        dataList.add("添加城市");
        if (null != addCounties && addCounties.size() > 0) {
            for (AddCounty addCounty : addCounties) {
                dataList.add(addCounty.getCountyName());
            }
            adapter.setIsvisiable(true);
            adapter.notifyDataSetChanged();
            areaRecyclerView.scrollToPosition(0);
            currentLevel = LEVEL_MANAGEMENT;
        } else {
            Toast.makeText(getContext(), "请添加城市", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 加载全国所有的省，优先才数据库查询，如果没有查询到再去服务器上获取
     */
    public void queryProvinces() {
        titleText.setText("中国");
        adapter.setIsvisiable(false);
        backButton.setVisibility(View.VISIBLE);
        locationIvObj.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
//        Log.i("test","queryProvinces1");
        if (null != provinceList && provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
//                Log.i("test",province.getProvinceName());
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            areaRecyclerView.scrollToPosition(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http:guolin.tech/api/china";
            queryFromServer(address, "province");
        }
//        Log.i("test","queryProvinces2");
    }

    /**
     * 加载选中省内所有的市，优先才数据库查询，如果没有查询到再去服务器上获取
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        locationIvObj.setVisibility(View.GONE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        if (null != cityList && cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
//            areaRecyclerView.setSelection(0);
            areaRecyclerView.scrollToPosition(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 加载选中市内所有的县，优先才数据库查询，如果没有查询到再去服务器上获取
     */
    private void queryCounties() {
        titleText.setText(selecetedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        locationIvObj.setVisibility(View.GONE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selecetedCity.getCityCode())).find(County.class);
        if (null != countyList && countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
//            areaRecyclerView.setSelection(0);
            areaRecyclerView.scrollToPosition(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selecetedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     *
     * @param address 地址
     * @param type    类型
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getProvinceCode());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selecetedCity.getCityCode());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*
    * 显示进度对话框
    * */
    private void showProgressDialog() {
        if (null == progressDialog) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*
    * 关闭进度对话框
    * */
    private void closeProgressDialog() {
        if (null != progressDialog) {
            progressDialog.dismiss();
        }
    }
}
