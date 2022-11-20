package pl.krystianzak.to_do_list;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.navigation.NavigationView;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pl.krystianzak.to_do_list.databinding.ActivityMainBinding;
import pl.krystianzak.to_do_list.ui.MainAdapter;
import pl.krystianzak.to_do_list.ui.RoomDB;
import pl.krystianzak.to_do_list.ui.all.AllFragment;

public class MainActivity extends AppCompatActivity implements MainCallbacks {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    Context context;
    final int mode = Activity.MODE_PRIVATE;
    //    create a reference to the shared preferences object
    SharedPreferences mySharedPreferences;
    //    obtain an editor to add data to my SharedPreferences object
    SharedPreferences.Editor myEditor;

    ImageView imageView;
    RadioGroup radioGroup;

    FragmentTransaction fragmentTransaction;
    AllFragment allFragment;

    RoomDB roomDataBase;
    List<JobData> jobsList = new ArrayList<>();
    MainAdapter mainAdapter = null;

    Integer typeOfJobList = 0;

    String sortVariable = "jobStartDateTime";
    Boolean sortType = false; // false - ASC | true - DESC
    Integer sortNumber = 6;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializePreferences();

        //Initialize database
        roomDataBase = RoomDB.getInstance(this);
        //Store database value in data list by updateJobList method
        updateJobList();

        //Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;

        radioGroup = (RadioGroup) findViewById(R.id.radioGroupNewJobPriority);
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewJobDialog();
            }
        });


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_done, R.id.nav_all, R.id.nav_about, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (mySharedPreferences.getBoolean("nightMode", false))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Initialize background service 
        //scheduleJob();
        if (mySharedPreferences.getBoolean("enableNotify", true))
            scheduleWorker();
    }

    public void scheduleWorker() {
        final PeriodicWorkRequest periodicWorkRequest1 = new PeriodicWorkRequest.Builder(NotificationWorker.class, mySharedPreferences.getInt("notifyTime", 15), TimeUnit.MINUTES)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build();

        WorkManager workManager = WorkManager.getInstance(this);

        workManager.enqueue(periodicWorkRequest1);

        workManager.getWorkInfoByIdLiveData(periodicWorkRequest1.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null) {
                            //Log.e("JOB", "Status changed to : " + workInfo.getState());

                        }
                    }
                });
    }

   /* public static final int jobId = 524;

    public void scheduleJob() {

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (scheduler.getPendingJob(jobId) == null) {

            ComponentName componentName = new ComponentName(this, NotificationService.class);
            int minute = 15;
            JobInfo.Builder info = new JobInfo.Builder(jobId, componentName)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    .setPersisted(true)
                    .setMinimumLatency(1 * 1000)
                    .setOverrideDeadline(3 * 1000)
                    //.setPeriodic(minute * 60 * 1000)
                    //.build()
                    ;

            int resultCode = scheduler.schedule(info.build());
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                //Log.e("JOB", "Job scheduled");
            } else {
                //Log.e("JOB", "Job scheduling failed");

            }
        } else {

            //Log.e("JOB", "Job service is already scheduled");
        }
    }*/

    public void cancelJob() {
        //JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        //scheduler.cancel(jobId);
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.cancelAllWork();
        //Log.e("JOB", "Job cancelled");

    }


    @Override
    public void onMsgFromFragToMain(String sender, String strValue) {
        Toast.makeText(getApplication(),
                " MAIN GOT>> " + sender + "\n" + strValue, Toast.LENGTH_LONG).show();
        if (sender.equals("BLUE")) {
            try {
                //Log.e("aaa", "onStrFromFragToMain " + strValue);
            } catch (Exception e) {
                //Log.e("ERROR", "onStrFromFragToMain " + e.getMessage());
            }
        }
    }

    public void initializePreferences() {

        // PREFERENCES
        mySharedPreferences = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        myEditor = mySharedPreferences.edit();
        if (mySharedPreferences != null && mySharedPreferences.contains("defaultPriority") && mySharedPreferences.contains("nightMode")
                && mySharedPreferences.contains("notifyTime") && mySharedPreferences.contains("easyNotify") && mySharedPreferences.contains("mediumNotify") && mySharedPreferences.contains("hardNotify")) {

        } else {
            //pierwsze uruchomienie
            myEditor.clear();
            myEditor.putInt("defaultPriority", 1);
            myEditor.putBoolean("nightMode", false);
            myEditor.putInt("notifyTime", 15);
            myEditor.putBoolean("enableNotify", true);
            myEditor.putBoolean("easyNotify", false);
            myEditor.putBoolean("mediumNotify", false);
            myEditor.putBoolean("hardNotify", true);
            myEditor.apply();
        }
    }

    @Override
    protected void onPause() {
        myEditor.commit();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Nazwa zadania...");

        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setHintTextColor(Color.parseColor("#5FFFFFFF"));
        searchAutoComplete.setTextColor(Color.WHITE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (mainAdapter != null)
                    mainAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mainAdapter != null)
                    mainAdapter.filter(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                showSortDialog();
                break;
            case R.id.action_mark_all_done:
                //imageChooser();
                roomDataBase.mainDao().updateStatusAll(1);
                refreshListAndAdapter();
                Toast.makeText(context, getResources().getString(R.string.changed_all_done), Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_mark_all_progress:
                roomDataBase.mainDao().updateStatusAll(0);
                refreshListAndAdapter();
                Toast.makeText(context, getResources().getString(R.string.changed_all_progress), Toast.LENGTH_SHORT).show();
                break;
            //Refresh recycleview
            case R.id.action_refresh:
                refreshListAndAdapter();
                Toast.makeText(context, getResources().getString(R.string.list_refreshed), Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshListAndAdapter() {
        updateJobList();
        mainAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    int pickerHour, pickerMinute, priority;

    public void showSortDialog() {
        final Dialog sortDialog = new Dialog(context);
        sortDialog.setTitle(getResources().getString(R.string.dialog_sort_title));
        sortDialog.setContentView(R.layout.dialog_sort);
        final Spinner spinnerSort = sortDialog.findViewById(R.id.spinnerSort);
        final Button btnSort = sortDialog.findViewById(R.id.buttonSort);
        final Button btnCancel = sortDialog.findViewById(R.id.buttonSortCancel);

        ArrayAdapter<String> spinnerSortAdapter;
        String[] spinnerItems = {"Nazwa malejąco", "Nazwa rosnąco",
                "Priorytet malejąco", "Priorytet rosnąco",
                "Data i czas zakończenia malejąco", "Data i czas zakończenia rosnąco",
                "Data i czas rozpoczęcia malejąco", "Data i czas rozpoczęcia rosnąco",
                "Status wykonania malejąco", "Status wykonania rosnąco"};
        spinnerSortAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerItems
        );
        spinnerSort.setAdapter(spinnerSortAdapter);
        spinnerSort.setSelection(sortNumber);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortDialog.dismiss();
            }
        });

        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerSort.getSelectedItemPosition() == 0) {
                    sortVariable = "jobName";
                    sortType = true;
                    sortNumber = 0;
                } else if (spinnerSort.getSelectedItemPosition() == 1) {
                    sortVariable = "jobName";
                    sortType = false;
                    sortNumber = 1;
                } else if (spinnerSort.getSelectedItemPosition() == 2) {
                    sortVariable = "jobPriority";
                    sortType = true;
                    sortNumber = 2;
                } else if (spinnerSort.getSelectedItemPosition() == 3) {
                    sortVariable = "jobPriority";
                    sortType = false;
                    sortNumber = 3;
                } else if (spinnerSort.getSelectedItemPosition() == 4) {
                    sortVariable = "jobEndDateTime";
                    sortType = true;
                    sortNumber = 4;
                } else if (spinnerSort.getSelectedItemPosition() == 5) {
                    sortVariable = "jobEndDateTime";
                    sortType = false;
                    sortNumber = 5;
                } else if (spinnerSort.getSelectedItemPosition() == 6) {
                    sortVariable = "jobStartDateTime";
                    sortType = true;
                    sortNumber = 6;
                } else if (spinnerSort.getSelectedItemPosition() == 7) {
                    sortVariable = "jjobStartDateTime";
                    sortType = false;
                    sortNumber = 7;
                } else if (spinnerSort.getSelectedItemPosition() == 8) {
                    sortVariable = "jobStatus";
                    sortType = true;
                    sortNumber = 8;
                } else if (spinnerSort.getSelectedItemPosition() == 9) {
                    sortVariable = "jobStatus";
                    sortType = false;
                    sortNumber = 9;
                }
                refreshListAndAdapter();
                Toast.makeText(context, getResources().getString(R.string.dialog_sort_result) + " " + spinnerItems[spinnerSort.getSelectedItemPosition()], Toast.LENGTH_SHORT).show();
                sortDialog.dismiss();
            }
        });

        sortDialog.show();
    }

    public void showNewJobDialog() {
        final Dialog newJobDialog = new Dialog(context);
        newJobDialog.setTitle("Dodaj nowe zadanie");

        newJobDialog.setContentView(R.layout.dialog_new_job);

        final RadioButton newJobPriorityEasy = newJobDialog.findViewById(R.id.radioButtonEasy);
        final RadioButton newJobPriorityMedium = newJobDialog.findViewById(R.id.radioButtonMedium);
        final RadioButton newJobPriorityHard = newJobDialog.findViewById(R.id.radioButtonHard);
        final Button btnNewJobSelectEndDate = newJobDialog.findViewById(R.id.buttonNewJobSelectEndDate);
        final Button btnNewJobSelectTime = newJobDialog.findViewById(R.id.buttonNewJobSelectTime);

        Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);

                String myFormat = "dd/MM/YYYY";
                SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat);
                btnNewJobSelectEndDate.setText(dateFormat.format(myCalendar.getTime()));

            }
        };

        btnNewJobSelectEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        btnNewJobSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        pickerHour = hourOfDay;
                        pickerMinute = minute;
                        btnNewJobSelectTime.setText(String.format(Locale.getDefault(), "%02d:%02d", pickerHour, pickerMinute));
                    }
                };
                int style = AlertDialog.THEME_HOLO_DARK;
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, style, onTimeSetListener, pickerHour, pickerMinute, true);
                timePickerDialog.setTitle(getString(R.string.dialog_job_time_select));
                timePickerDialog.show();

            }
        });

        newJobDialog.findViewById(R.id.buttonNewJobCancel).setOnClickListener(v -> {
            newJobDialog.dismiss();
        });

        newJobDialog.show();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(newJobDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        //layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        newJobDialog.getWindow().setAttributes(layoutParams);

        switch (mySharedPreferences.getInt("defaultPriority", 1)) {
            case 0:
                ((RadioButton) ((RadioGroup) newJobDialog.findViewById(R.id.radioGroupNewJobPriority)).getChildAt(0)).setChecked(true);
                break;
            case 1:
                ((RadioButton) ((RadioGroup) newJobDialog.findViewById(R.id.radioGroupNewJobPriority)).getChildAt(1)).setChecked(true);
                break;
            case 2:
                ((RadioButton) ((RadioGroup) newJobDialog.findViewById(R.id.radioGroupNewJobPriority)).getChildAt(2)).setChecked(true);
                break;
            default:
                ((RadioButton) ((RadioGroup) newJobDialog.findViewById(R.id.radioGroupNewJobPriority)).getChildAt(0)).setChecked(true);
                break;
        }

        newJobDialog.findViewById(R.id.buttonNewJobAdd).setOnClickListener(v -> {
            String name = ((EditText) newJobDialog.findViewById(R.id.editTextNewJobName)).getText().toString().trim();
            String notes = ((EditText) newJobDialog.findViewById(R.id.editTextNewJobDescription)).getText().toString().trim();

            // Format actual datatime
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
            df.setTimeZone(tz);
            String nowAsISO = df.format(new Date());

            String myFormat = "yyyy-MM-dd";
            SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat);

            Boolean er = false;
            String msg = "";
            if (TextUtils.isEmpty(name)) {
                msg = getResources().getString(R.string.nameIsEmpty) + '\n';
                er = true;
            } else if (name.length() > 40) {
                msg = getResources().getString(R.string.nameIsTooLong) + '\n';
                er = true;
            }
            String endDateAsIso = "";
            if (myCalendar.getTime() != null && !TextUtils.isEmpty(Integer.toString(pickerHour)) && !TextUtils.isEmpty(Integer.toString(pickerMinute))) {
                endDateAsIso = dateFormat.format(myCalendar.getTime()) + "_" + String.format(Locale.getDefault(), "%02d:%02d", pickerHour, pickerMinute);
            } else {
                msg = getResources().getString(R.string.endDateTimeIsEmpty) + '\n';
                er = true;
            }


            if (!er) {
                priority = ((RadioGroup) newJobDialog.findViewById(R.id.radioGroupNewJobPriority)).getCheckedRadioButtonId();

                if (priority == newJobPriorityEasy.getId()) {
                    priority = 0;
                } else if (priority == newJobPriorityMedium.getId()) {
                    priority = 1;
                } else if (priority == newJobPriorityHard.getId()) {
                    priority = 2;
                }
                JobData data = new JobData();
                data.setName(name);
                data.setNote(TextUtils.isEmpty(notes) ? "" : notes);

                data.setEndDateTime(endDateAsIso);
                data.setStartDateTime(nowAsISO);
                data.setPriority(priority);
                data.setStatus(0);

                //Insert text in database
                roomDataBase.mainDao().insert(data);
                updateJobList();
                if (mainAdapter != null) {
                    mainAdapter.notifyDataSetChanged();
                }
                Toast.makeText(context, getResources().getString(R.string.dialog_job_added), Toast.LENGTH_SHORT).show();
                newJobDialog.dismiss();
            } else {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }

        });

    }

    void imageChooser() {
        someActivityResultLauncher.launch("image/*");
    }

    ActivityResultLauncher<String> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {

                    //((TextView) findViewById(R.id.text_home)).setText(uri.toString() + "");
                    if (null != uri) {
                        // update the preview image in the layout
                        // imageView.setImageURI(uri);
                    }
                }
            });

    @Override
    public void onResume() {
        super.onResume();
        updateJobList();
        if (mainAdapter != null)
            mainAdapter.notifyDataSetChanged();
    }

    public void updateJobList() {
        jobsList.clear();
        String sortT = sortType ? "DESC" : "ASC";
        switch (typeOfJobList) {
            case 1:
                jobsList.addAll(roomDataBase.mainDao().getJobsRaw(new SimpleSQLiteQuery("SELECT * FROM job_table WHERE jobStatus = 1 ORDER BY " + sortVariable + " " + sortT)));
                break;
            case 2:
                jobsList.addAll(roomDataBase.mainDao().getJobsRaw(new SimpleSQLiteQuery("SELECT * FROM job_table WHERE jobStatus = 0 ORDER BY " + sortVariable + " " + sortT)));
                break;
            default:
                jobsList.addAll(roomDataBase.mainDao().getJobsRaw(new SimpleSQLiteQuery("SELECT * FROM job_table ORDER BY " + sortVariable + " " + sortT)));
        }
        if (mainAdapter != null)
            mainAdapter.copyDataList();
    }

    public Integer getTypeOfJobList() {
        return typeOfJobList;
    }

    public void setTypeOfJobList(Integer typeOfJobList) {
        this.typeOfJobList = typeOfJobList;
    }

    public String getSortVariable() {
        return sortVariable;
    }

    public void setSortVariable(String sortVariable) {
        this.sortVariable = sortVariable;
    }

    public Boolean getSortType() {
        return sortType;
    }

    public void setSortType(Boolean sortType) {
        this.sortType = sortType;
    }

    public RoomDB getRoomDataBase() {
        return roomDataBase;
    }

    public void setRoomDataBase(RoomDB roomDataBase) {
        this.roomDataBase = roomDataBase;
    }

    public List<JobData> getJobsList() {
        return jobsList;
    }

    public void setJobsList(List<JobData> jobsList) {
        this.jobsList = jobsList;
    }

    public SharedPreferences getMySharedPreferences() {
        return mySharedPreferences;
    }

    public void setMySharedPreferences(SharedPreferences mySharedPreferences) {
        this.mySharedPreferences = mySharedPreferences;
    }

    public SharedPreferences.Editor getMyEditor() {
        return myEditor;
    }

    public void setMyEditor(SharedPreferences.Editor myEditor) {
        this.myEditor = myEditor;
    }

    public MainAdapter getMainAdapter() {
        return mainAdapter;
    }

    public void setMainAdapter(MainAdapter mainAdapter) {
        this.mainAdapter = mainAdapter;
    }
}