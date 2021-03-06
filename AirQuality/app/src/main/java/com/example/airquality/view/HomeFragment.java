package com.example.airquality.view;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.airquality.Adapters.DayAdapter;
import com.example.airquality.Adapters.HourAdapter;
import com.example.airquality.Adapters.SpinnerAdapter;
import com.example.airquality.AppDatabase;
import com.example.airquality.R;
import com.example.airquality.databinding.FragmentHomeBinding;
import com.example.airquality.model.DailyAirQuality;
import com.example.airquality.model.HourlyAirQuality;
import com.example.airquality.model.Location;
import com.example.airquality.viewmodel.DailyAirQualityDAO;
import com.example.airquality.viewmodel.HourlyAirQualityDAO;
import com.example.airquality.viewmodel.LocationDAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    private Location currentLocation;
    private LocationDAO locationDAO;
    private ArrayList<Location> locationArrayList;

    private HourlyAirQualityDAO hourlyAirQualityDAO;
    private ArrayList<HourlyAirQuality> hourArrayList;
    private HourlyAirQuality currentHourlyData;

    private ArrayList<DailyAirQuality> dayArrayList;

    private AppDatabase appDatabase;
    private FragmentHomeBinding binding;

    private String stringDay = "";
    private String stringDayHour = "";

    @Override
    public void onCreate(Bundle bundle) {
        setHasOptionsMenu(true);
        super.onCreate(bundle);
        if (getArguments() != null) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        stringDay = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        stringDayHour = new SimpleDateFormat("dd/MM/yyyy HH:00:00").format(new Date());

        // Hourly air quality list (Horizontal)
        binding.tbHome.inflateMenu(R.menu.menu_home);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.rvHours.setLayoutManager(layoutManager);
        binding.rvDays.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up app database
        appDatabase = AppDatabase.Instance(requireContext().getApplicationContext());
        locationDAO = appDatabase.locationDAO();
        hourlyAirQualityDAO = appDatabase.hourlyAirQualityDAO();

        // Get UI data
        hourArrayList = new ArrayList<HourlyAirQuality>();
        dayArrayList = new ArrayList<DailyAirQuality>();
        locationArrayList = new ArrayList<Location>();

        locationArrayList.addAll(locationDAO.getAll());
        currentLocation = locationArrayList.get(0);

        // Setup spinner data
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(requireContext(), R.layout.spinner_items_category, locationArrayList);
        binding.snLocation.setAdapter(spinnerAdapter);
        binding.snLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentLocation = locationArrayList.get(i);
                loadHome();
                loadHours();
                loadDays();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Get first marked station to load to UI if there is no marked and favourite location
        Location favouriteLocation = locationDAO.getFavouriteLocation();
        if (favouriteLocation == null){
            List<Location> markedLocation = locationDAO.getListHasMark();
            if (markedLocation.size() == 0){
                binding.snLocation.setSelection(0);
            }
            else {
                binding.snLocation.setSelection(markedLocation.get(0).getId() - 1);
            }
        }
        else binding.snLocation.setSelection(favouriteLocation.getId() - 1);


        List<HourlyAirQuality> currentDateData = hourlyAirQualityDAO.getListByLocationIDAndDate(currentLocation.getId(), stringDay);
        if (currentDateData.size() > 1){
            currentHourlyData = currentDateData.get(currentDateData.size() - 1);
        }

        loadHome();
        loadHours();
        loadDays();
    }

    @SuppressLint("DefaultLocale")
    private void loadHome() {
        // Card view top
        binding.tvLocation.setText(currentLocation.getStationName());
        binding.tvRate.setText(currentLocation.getRated());

        setBackgroundColor(currentLocation.getRated());
        binding.tvAqi.setText((int)currentLocation.getAqi()+"");

        // Set up air quality info bottom
        if (currentHourlyData != null) {
            setText(currentHourlyData);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadHours() {
        // On click listener go to hour detail fragment
        HourAdapter.HourClickListener hourClickListener = new HourAdapter.HourClickListener() {
            @Override
            public void onCLick(View view, int i) {
                Bundle bundle = new Bundle();
                HourlyAirQuality hour = hourArrayList.get(i);
                bundle.putString("Date-LocationID", hour.getDatetime() + "," + hour.getLocationID());
                HourDetailFragment hourDetailFragment = new HourDetailFragment();
                hourDetailFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_home, hourDetailFragment).addToBackStack(null).commit();
            }
        };

        // Get hourly data of current station
        hourArrayList.clear();
        HourAdapter hoursAdapter = new HourAdapter(hourArrayList, hourClickListener);
        hourArrayList.addAll(hourlyAirQualityDAO.getListByLocationIDAndDate(currentLocation.getId(), stringDay));
        Collections.reverse(hourArrayList);
        hoursAdapter.notifyDataSetChanged();
        binding.rvHours.setAdapter(hoursAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadDays() {
        // On click listener go to day detail fragment
        DayAdapter.DayClickListener dayClickListener = new DayAdapter.DayClickListener() {
            @Override
            public void onCLick(View view, int i) {
                Bundle bundle = new Bundle();
                DailyAirQuality day = dayArrayList.get(i);
                bundle.putString("Date-LocationID", day.getDate() + "," + day.getLocationID());
                DayDetailFragment dayDetailFragment = new DayDetailFragment();
                dayDetailFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_home, dayDetailFragment).addToBackStack(null).commit();
            }
        };

        // Get daily data of current station
        dayArrayList.clear();
        DayAdapter daysAdapter = new DayAdapter(dayArrayList, dayClickListener);
        DailyAirQualityDAO dailyAirQualityDAO = appDatabase.dailyAirQualityDAO();
        dayArrayList.addAll(dailyAirQualityDAO.getListByLocationID(currentLocation.getId()));
        daysAdapter.notifyDataSetChanged();
        binding.rvDays.setAdapter(daysAdapter);
    }

    private void setBackgroundColor(String rate) {
        switch (rate) {
        case "T???t": // Xanh l??
            binding.fmHome.setBackgroundResource(R.drawable.custom_background_green);
            binding.llAvatar.setBackgroundResource(R.color.green);
            binding.imAvatar.setImageResource(R.drawable.avatar_green);
            binding.llText.setBackgroundResource(R.color.light_green);
            binding.tvRecommended.setText("Kh??ng ???nh h?????ng t???i s???c kh???e");
            break;
        case "Trung b??nh": // V??ng
            binding.fmHome.setBackgroundResource(R.drawable.custom_background_yellow);
            binding.llAvatar.setBackgroundResource(R.color.yellow);
            binding.imAvatar.setImageResource(R.drawable.avatar_yellow);
            binding.llText.setBackgroundResource(R.drawable.custom_yellow);
            binding.tvRecommended.setText("Nh??m nh???y c???m n??n h???n ch??? th???i gian ??? b??n ngo??i");
            break;
        case "K??m": // Cam
            binding.fmHome.setBackgroundResource(R.drawable.custom_background_orange);
            binding.llAvatar.setBackgroundResource(R.color.orange);
            binding.imAvatar.setImageResource(R.drawable.avatar_orange);
            binding.llText.setBackgroundResource(R.drawable.custom_orange);
            binding.tvRecommended.setText("Nh??m nh???y c???m h???n ch??? th???i gian ??? b??n ngo??i");
            break;
        case "X???u": // ?????
            binding.fmHome.setBackgroundResource(R.drawable.custom_background_red);
            binding.llAvatar.setBackgroundResource(R.color.red);
            binding.imAvatar.setImageResource(R.drawable.avatar_red);
            binding.llText.setBackgroundResource(R.drawable.custom_red);
            binding.tvRecommended.setText("Nh??m nh???y c???m tr??nh ra ngo??i. Nh???ng ng?????i kh??c h???n ch??? ra ngo??i");
            break;
        case "R???t x???u": // T??m
            binding.fmHome.setBackgroundResource(R.drawable.custom_background_purple);
            binding.llAvatar.setBackgroundResource(R.color.purple);
            binding.imAvatar.setImageResource(R.drawable.avatar_purple);
            binding.llText.setBackgroundResource(R.drawable.custom_purple);
            binding.tvRecommended.setText("Nh??m nh???y c???m tr??nh ra ngo??i. Nh???ng ng?????i kh??c h???n ch??? ra ngo??i");
            break;
        case "Nguy h???i": // N??u
            binding.fmHome.setBackgroundResource(R.drawable.custom_background_brown);
            binding.llAvatar.setBackgroundResource(R.color.brown);
            binding.imAvatar.setImageResource(R.drawable.avatar_brown);
            binding.llText.setBackgroundResource(R.drawable.custom_brown);
            binding.tvRecommended.setText("M???i ng?????i n??n ??? nh??");
            break;
        }
    }

    private void setText(HourlyAirQuality hourlyAirQuality) {
        binding.tvPM25.setText(String.format("%.1f",hourlyAirQuality.getPm25()));
        setViewColorPM25(hourlyAirQuality.getPm25());
        binding.tvPM10.setText(String.format("%.1f",hourlyAirQuality.getPm10()));
        setViewColorPM10(hourlyAirQuality.getPm10());
        binding.tvO3.setText(String.format("%.1f",hourlyAirQuality.getO3()));
        setViewColorO3(hourlyAirQuality.getO3());
        binding.tvNO2.setText(String.format("%.1f",hourlyAirQuality.getNo2()));
        setViewColorNO2(hourlyAirQuality.getNo2());
        binding.tvSO2.setText(String.format("%.1f",hourlyAirQuality.getSo2()));
        setViewColorSO2(hourlyAirQuality.getSo2());
        binding.tvCO.setText(String.format("%.1f",hourlyAirQuality.getCo()));
        setViewColorCO(hourlyAirQuality.getCo());
        binding.tvNO.setText(String.format("%.1f",hourlyAirQuality.getNo()));
        setViewColorNO(hourlyAirQuality.getNo());
        binding.tvNOx.setText(String.format("%.1f",hourlyAirQuality.getNox()));
        setViewColorNOx(hourlyAirQuality.getNox());
        binding.tvNH3.setText(String.format("%.1f",hourlyAirQuality.getNh3()));
        setViewColorNH3(hourlyAirQuality.getNh3());
        binding.tvBenzen.setText(String.format("%.1f",hourlyAirQuality.getBenzene()));
        setViewColorBenzen(hourlyAirQuality.getBenzene());
        binding.tvToluene.setText(String.format("%.1f",hourlyAirQuality.getToluene()));
        setViewColorToluene(hourlyAirQuality.getToluene());
        binding.tvXylene.setText(String.format("%.1f",hourlyAirQuality.getXylene()));
        setViewColorXylene(hourlyAirQuality.getXylene());
    }

    private void setViewColorPM25(double pm25) {
        if(pm25<=30) binding.viewPM25.setBackgroundColor(getResources().getColor(R.color.green));
        else if(pm25<=60) binding.viewPM25.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(pm25<=90) binding.viewPM25.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(pm25<=120) binding.viewPM25.setBackgroundColor(getResources().getColor(R.color.red));
        else if(pm25<=250) binding.viewPM25.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewPM25.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorPM10(double pm10) {
        if(pm10<=50) binding.viewPM10.setBackgroundColor(getResources().getColor(R.color.green));
        else if(pm10<=100) binding.viewPM10.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(pm10<=250) binding.viewPM10.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(pm10<=350) binding.viewPM10.setBackgroundColor(getResources().getColor(R.color.red));
        else if(pm10<=430) binding.viewPM10.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewPM10.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorO3(double o3) {
        if(o3<=50) binding.viewO3.setBackgroundColor(getResources().getColor(R.color.green));
        else if(o3<=100) binding.viewO3.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(o3<=168) binding.viewO3.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(o3<=208) binding.viewO3.setBackgroundColor(getResources().getColor(R.color.red));
        else if(o3<=748) binding.viewO3.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewO3.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorNO2(double no2) {
        if(no2<=40) binding.viewNO2.setBackgroundColor(getResources().getColor(R.color.green));
        else if(no2<=80) binding.viewNO2.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(no2<=180) binding.viewNO2.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(no2<=280) binding.viewNO2.setBackgroundColor(getResources().getColor(R.color.red));
        else if(no2<=400) binding.viewNO2.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewNO2.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorSO2(double so2) {
        if(so2<=40) binding.viewSO2.setBackgroundColor(getResources().getColor(R.color.green));
        else if(so2<=80) binding.viewSO2.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(so2<=380) binding.viewSO2.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(so2<=800) binding.viewSO2.setBackgroundColor(getResources().getColor(R.color.red));
        else if(so2<=1600) binding.viewSO2.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewSO2.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorCO(double co) {
        if(co<=1) binding.viewCO.setBackgroundColor(getResources().getColor(R.color.green));
        else if(co<=2) binding.viewCO.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(co<=10) binding.viewCO.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(co<=17) binding.viewCO.setBackgroundColor(getResources().getColor(R.color.red));
        else if(co<=34) binding.viewCO.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewCO.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorNO(double no) {
        if(no<=200) binding.viewNO.setBackgroundColor(getResources().getColor(R.color.green));
        else if(no<=400) binding.viewNO.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(no<=800) binding.viewNO.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(no<=1200) binding.viewNO.setBackgroundColor(getResources().getColor(R.color.red));
        else if(no<=1800) binding.viewNO.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewNO.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorNOx(double nox) {
        if(nox<=200) binding.viewNOx.setBackgroundColor(getResources().getColor(R.color.green));
        else if(nox<=400) binding.viewNOx.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(nox<=800) binding.viewNOx.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(nox<=1200) binding.viewNOx.setBackgroundColor(getResources().getColor(R.color.red));
        else if(nox<=1800) binding.viewNOx.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewNOx.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorNH3(double nh3) {
        if(nh3<=200) binding.viewNH3.setBackgroundColor(getResources().getColor(R.color.green));
        else if(nh3<=400) binding.viewNH3.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(nh3<=800) binding.viewNH3.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(nh3<=1200) binding.viewNH3.setBackgroundColor(getResources().getColor(R.color.red));
        else if(nh3<=1800) binding.viewNH3.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewNH3.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorBenzen(double benzen) {
        if(benzen<=40) binding.viewBenzen.setBackgroundColor(getResources().getColor(R.color.green));
        else if(benzen<=80) binding.viewBenzen.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(benzen<=380) binding.viewBenzen.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(benzen<=800) binding.viewBenzen.setBackgroundColor(getResources().getColor(R.color.red));
        else if(benzen<=1600) binding.viewBenzen.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewBenzen.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorToluene(double toluene) {
        if(toluene<=40) binding.viewToluene.setBackgroundColor(getResources().getColor(R.color.green));
        else if(toluene<=80) binding.viewToluene.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(toluene<=380) binding.viewToluene.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(toluene<=800) binding.viewToluene.setBackgroundColor(getResources().getColor(R.color.red));
        else if(toluene<=1600) binding.viewToluene.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewToluene.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    private void setViewColorXylene(double xylene) {
        if(xylene<=10) binding.viewXylene.setBackgroundColor(getResources().getColor(R.color.green));
        else if(xylene<=80) binding.viewXylene.setBackgroundColor(getResources().getColor(R.color.yellow));
        else if(xylene<=300) binding.viewXylene.setBackgroundColor(getResources().getColor(R.color.orange));
        else if(xylene<=800) binding.viewXylene.setBackgroundColor(getResources().getColor(R.color.red));
        else if(xylene<=1600) binding.viewXylene.setBackgroundColor(getResources().getColor(R.color.purple));
        else binding.viewXylene.setBackgroundColor(getResources().getColor(R.color.brown));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}