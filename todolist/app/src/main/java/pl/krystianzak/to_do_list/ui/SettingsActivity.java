package pl.krystianzak.to_do_list.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pl.krystianzak.to_do_list.JobData;
import pl.krystianzak.to_do_list.NotificationService;
import pl.krystianzak.to_do_list.NotificationWorker;
import pl.krystianzak.to_do_list.R;
import pl.krystianzak.to_do_list.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivitySettingsBinding binding;
    private RadioGroup radioGroup;
    private Button btnDatabaseExport, btnDatabaseImport, btnNotificationSystem, btnDatabaseReset;
    private Switch switchNightMode, switchNotifyEnable;
    private Spinner spinnerNotifyTime;
    private CheckBox checkBoxNotifyEasy, checkBoxNotifyMedium, checkBoxNotifyHard;
    Context context;

    public final static String DB_NAME = "jobs_db";
    //    create a reference to the shared preferences object
    SharedPreferences mySharedPreferences;
    //    obtain an editor to add data to my SharedPreferences object
    SharedPreferences.Editor myEditor;

    RoomDB roomDataBase;
    List<JobData> jobsList = new ArrayList<>();
    ArrayAdapter<String> spinnerNotifyTimeAdapter;

    public SettingsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mySharedPreferences = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        myEditor = mySharedPreferences.edit();

        //Initialize database
        roomDataBase = RoomDB.getInstance(this);
        //Store database value in data list
        jobsList = roomDataBase.mainDao().getAll();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        context = this;

        switchNightMode = (Switch) findViewById(R.id.switchNightMode);
        switchNotifyEnable = (Switch) findViewById(R.id.switchEnableNotify);
        spinnerNotifyTime = (Spinner) findViewById(R.id.spinnerSort);

        checkBoxNotifyEasy = (CheckBox) findViewById(R.id.checkBoxPriorityEasy);
        checkBoxNotifyMedium = (CheckBox) findViewById(R.id.checkBoxPriorityMedium);
        checkBoxNotifyHard = (CheckBox) findViewById(R.id.checkBoxPriorityHard);
        checkBoxNotifyEasy.setChecked(mySharedPreferences.getBoolean("easyNotify", false));
        checkBoxNotifyMedium.setChecked(mySharedPreferences.getBoolean("mediumNotify", false));
        checkBoxNotifyHard.setChecked(mySharedPreferences.getBoolean("hardNotify", true));

        btnDatabaseExport = (Button) findViewById(R.id.buttonDatabaseExport);
        btnDatabaseImport = (Button) findViewById(R.id.buttonDatabaseImport);
        btnNotificationSystem = (Button) findViewById(R.id.buttonNotificationSystem);
        btnDatabaseReset = findViewById(R.id.buttonDatabaseReset);

        btnDatabaseExport.setOnClickListener(this);
        btnDatabaseImport.setOnClickListener(this);
        btnNotificationSystem.setOnClickListener(this);
        btnDatabaseReset.setOnClickListener(this);

        checkBoxNotifyEasy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myEditor.putBoolean("easyNotify", isChecked);
                myEditor.apply();
            }
        });
        checkBoxNotifyMedium.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myEditor.putBoolean("mediumNotify", isChecked);
                myEditor.apply();
            }
        });
        checkBoxNotifyHard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myEditor.putBoolean("hardNotify", isChecked);
                myEditor.apply();
            }
        });

        String[] spinnerItems = {"Co 15 minut", "Co 20 minut", "Co 30 minut", "Co 1 godzinę", "Co 2 godziny", "Co 4 godziny", "Co 6 godzin", "Co 12 godzin", "Co 24 godziny"};
        spinnerNotifyTimeAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerItems
        );
        spinnerNotifyTime.setAdapter(spinnerNotifyTimeAdapter);
        switch (mySharedPreferences.getInt("notifyTime", 15)) {
            case 20:
                spinnerNotifyTime.setSelection(1);
                break;
            case 30:
                spinnerNotifyTime.setSelection(2);
                break;
            case 60:
                spinnerNotifyTime.setSelection(3);
                break;
            case 120:
                spinnerNotifyTime.setSelection(4);
                break;
            case 240:
                spinnerNotifyTime.setSelection(5);
                break;
            case 360:
                spinnerNotifyTime.setSelection(6);
                break;
            case 720:
                spinnerNotifyTime.setSelection(7);
                break;
            case 1440:
                spinnerNotifyTime.setSelection(8);
                break;
            default:
                spinnerNotifyTime.setSelection(0);
                break;
        }
        spinnerNotifyTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        myEditor.putInt("notifyTime", 20);
                        break;
                    case 2:
                        myEditor.putInt("notifyTime", 30);
                        break;
                    case 3:
                        myEditor.putInt("notifyTime", 60);
                        break;
                    case 4:
                        myEditor.putInt("notifyTime", 120);
                        break;
                    case 5:
                        myEditor.putInt("notifyTime", 240);
                        break;
                    case 6:
                        myEditor.putInt("notifyTime", 360);
                        break;
                    case 7:
                        myEditor.putInt("notifyTime", 720);
                        break;
                    case 8:
                        myEditor.putInt("notifyTime", 1440);
                        break;
                    default:
                        myEditor.putInt("notifyTime", 15);
                        break;
                }

                myEditor.commit();
                cancelJob();
                scheduleWorker();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        switchNightMode.setChecked(mySharedPreferences.getBoolean("nightMode", false));
        switchNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myEditor.putBoolean("nightMode", isChecked);
                myEditor.commit();
                if (isChecked)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });


        switchNotifyEnable.setChecked(mySharedPreferences.getBoolean("enableNotify", true));
        /*switchNotifyEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myEditor.putBoolean("enableNotify", isChecked);
                myEditor.commit();
                if (isChecked) {
                    scheduleWorker();
                    Toast.makeText(context, getResources().getString(R.string.settings_notifyEnabled), Toast.LENGTH_SHORT).show();
                } else {
                    cancelJob();
                    Toast.makeText(context, getResources().getString(R.string.settings_notifyDisabled), Toast.LENGTH_SHORT).show();
                }

            }
        });*/

        switchNotifyEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isChecked = switchNotifyEnable.isChecked();
                myEditor.putBoolean("enableNotify", isChecked);
                myEditor.commit();
                if (isChecked) {
                    scheduleWorker();
                    Toast.makeText(context, getResources().getString(R.string.settings_notifyEnabled), Toast.LENGTH_SHORT).show();
                } else {
                    cancelJob();
                    Toast.makeText(context, getResources().getString(R.string.settings_notifyDisabled), Toast.LENGTH_SHORT).show();
                }
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.radioGroupDefaultDifficulty);
        switch (mySharedPreferences.getInt("defaultPriority", 0)) {
            case 0:
                ((RadioButton) findViewById(R.id.radioButtonEasy)).setChecked(true);
                break;
            case 1:
                ((RadioButton) findViewById(R.id.radioButtonMedium)).setChecked(true);
                break;
            case 2:
                ((RadioButton) findViewById(R.id.radioButtonHard)).setChecked(true);
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonEasy:
                        myEditor.putInt("defaultPriority", 0);
                        myEditor.commit();
                        break;
                    case R.id.radioButtonMedium:
                        myEditor.putInt("defaultPriority", 1);
                        myEditor.commit();
                        break;
                    case R.id.radioButtonHard:
                        myEditor.putInt("defaultPriority", 2);
                        myEditor.commit();
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonDatabaseExport:
                //Toast.makeText(getApplicationContext(), "export", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getResources().getString(R.string.settings_exportDB))
                        .setMessage(getResources().getString(R.string.settings_exportQuestion1) + getResources().getString(R.string.app_name) + "/" + DB_NAME + ".db ")
                        .setIcon(R.drawable.ic_baseline_info)
                        .setPositiveButton(getResources().getString(R.string.yes),
                                (dialog, which) -> exportDB(SettingsActivity.this))
                        .setNegativeButton(getResources().getString(R.string.no), null)
                        .create()
                        .show();

                break;
            case R.id.buttonDatabaseImport:
                //Toast.makeText(getApplicationContext(), "import", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getResources().getString(R.string.settings_importDB))
                        .setMessage(getResources().getString(R.string.settings_importQuestion1) + getResources().getString(R.string.app_name) + "/" + DB_NAME + ".db " + getResources().getString(R.string.settings_importQuestion2))
                        .setIcon(R.drawable.ic_baseline_info)
                        .setPositiveButton(getResources().getString(R.string.yes),
                                (dialog, which) -> importDB(SettingsActivity.this))
                        .setNegativeButton(getResources().getString(R.string.no), null)
                        .create()
                        .show();
                break;
            case R.id.buttonNotificationSystem:
                //Toast.makeText(getApplicationContext(),"powiadomienie",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                Context context = getApplicationContext();
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.putExtra("PUSH_SETTING_APP_PACKAGE", context.getPackageName());
                    intent.putExtra("PUSH_SETTING_APP_UID", context.getApplicationInfo().uid);
                } else {
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("PUSH_SETTING_URI_PACKAGE" + context.getPackageName()));
                }
                startActivity(intent);
                break;
            case R.id.buttonDatabaseReset:

                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getResources().getString(R.string.reset))
                        .setMessage(getResources().getString(R.string.reset_dialog))
                        .setIcon(R.drawable.ic_baseline_info)
                        .setPositiveButton(getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        roomDataBase.mainDao().reset(jobsList);
                                        jobsList.clear();
                                        jobsList.addAll(roomDataBase.mainDao().getAll());
                                        Toast.makeText(getApplicationContext(), "Baza danych została zresetowana", Toast.LENGTH_LONG).show();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.no), null)
                        .create()
                        .show();

                break;
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void exportDB(Context context) {
        //roomDataBase.close();
        roomDataBase.mainDao().checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));

        if (writeReadPerm()) {

            try {
                String appName = context.getResources().getString(R.string.app_name);
                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName + "/");
                if (!file.exists()) {
                    file.mkdirs();
                }

                File sd = Environment.getExternalStorageDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = getDatabasePath(DB_NAME).getAbsolutePath();
                    String backupDBPath = "/" + appName + "/" + DB_NAME + ".db";
                    File currentDB = new File(currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileInputStream src = new FileInputStream(currentDB);
                    FileOutputStream dst = new FileOutputStream(backupDB);

                    if (currentDB.exists()) {
                        FileChannel inChannel = src.getChannel();
                        FileChannel outChannel = dst.getChannel();
                        inChannel.transferTo(0, inChannel.size(), outChannel);

                        src.close();
                        dst.close();
                        Toast.makeText(context, context.getResources().getString(R.string.settings_exportSuccess), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.Error_NoDbFile), Toast.LENGTH_LONG).show();
                    }


                    ////Log.e("EXPORT_DB", "Database has been exported to\n" + backupDB.toString());
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.noStoragePermission), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                //e.printStackTrace();
                ////Log.e("EXPORT_DB", "Error exporting database! "+e.getMessage());
                Toast.makeText(context, context.getResources().getString(R.string.settings_exportError), Toast.LENGTH_LONG).show();
            }
        } else {
            requestWriteReadPerm();
        }

    }

    public void importDB(Context context) {
        roomDataBase.close();
        if (writeReadPerm()) {
            try {
                String appName = context.getResources().getString(R.string.app_name);
                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName + "/");
                if (!file.exists()) {
                    file.mkdirs();
                }

                File sd = Environment.getExternalStorageDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = getDatabasePath(DB_NAME).getAbsolutePath();
                    String backupDBPath = "/" + appName + "/" + DB_NAME + ".db";
                    File currentDB = new File(sd, backupDBPath);
                    File backupDB = new File(currentDBPath);

                    FileInputStream src = new FileInputStream(currentDB);
                    FileOutputStream dst = new FileOutputStream(backupDB);

                    if (currentDB.exists()) {
                        FileChannel inChannel = src.getChannel();
                        FileChannel outChannel = dst.getChannel();
                        inChannel.transferTo(0, inChannel.size(), outChannel);

                        src.close();
                        dst.close();
                        Toast.makeText(context, context.getResources().getString(R.string.settings_importSuccess), Toast.LENGTH_LONG).show();

                        System.exit(0);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.Error_NoDbFile), Toast.LENGTH_LONG).show();
                    }
                    ////Log.e("IMPORT_DB", "Database has been imported.");
                } else {
                    ////Log.e("IMPORT_DB", "No storage permission.");
                    Toast.makeText(context, context.getResources().getString(R.string.noStoragePermission), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ////Log.e("IMPORT_DB", "Error importing database! "+e.getMessage());
                Toast.makeText(context, context.getResources().getString(R.string.settings_importError), Toast.LENGTH_LONG).show();
            }

        } else {
            requestWriteReadPerm();
        }
    }

    public boolean writeReadPerm() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    public void requestWriteReadPerm() {
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(getResources().getString(R.string.settings_importExportPermissionTitle))
                .setMessage(getResources().getString(R.string.settings_importExportPermission))
                .setIcon(R.drawable.ic_baseline_info)
                .setPositiveButton(getResources().getString(R.string.yes),
                        (dialog, which) -> {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .create()
                .show();
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
                            //Log.e("JOB", "Status changed to : " + workInfo.getState() + " | Time: " + mySharedPreferences.getInt("notifyTime", 15));

                        }
                    }
                });
    }

    public void cancelJob() {
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.cancelAllWork();
        //Log.e("JOB", "Job cancelled");
    }
}