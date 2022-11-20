package pl.krystianzak.to_do_list;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.krystianzak.to_do_list.ui.RoomDB;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        //Log.e("JOB", "Job bg success");
        checkJobs();

        return Result.success();
    }

    void sendNotification(String title, String message) {
        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1",
                    "android",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("WorkManger");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "1")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))//message with line breaker
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void checkJobs() {
        RoomDB roomDataBase;
        List<JobData> jobsList = new ArrayList<>();
        SharedPreferences mySharedPreferences;

        mySharedPreferences = this.getApplicationContext().getSharedPreferences("settings", Activity.MODE_PRIVATE);
        //Initialize database
        roomDataBase = RoomDB.getInstance(this.getApplicationContext());
        //Store database value in data list
        jobsList = roomDataBase.mainDao().getAllProgress();

        String msg = "";
        Integer all = 0, easy = 0, medium = 0, hard = 0, overdue = 0;
        for (JobData job : jobsList) {
            if (mySharedPreferences.getBoolean("easyNotify", true) && job.getPriority() == 0) {
                all++;
                easy++;
                if (checkOverdueJob(job.getEndDateTime())) overdue++;
            }
            if (mySharedPreferences.getBoolean("mediumNotify", true) && job.getPriority() == 1) {
                all++;
                medium++;
                if (checkOverdueJob(job.getEndDateTime())) overdue++;
            }
            if (mySharedPreferences.getBoolean("hardNotify", true) && job.getPriority() == 2) {
                all++;
                hard++;
                if (checkOverdueJob(job.getEndDateTime())) overdue++;
            }
        }

        if (all > 0) {
            if (easy > 0) {
                msg += "Łatwych: " + easy + ". ";
            }
            if (medium > 0) {
                msg += "Średnich: " + medium + ". ";
            }
            if (hard > 0) {
                msg += "Trudnych: " + hard + ". ";
            }
            if (overdue > 0) {
                msg += "\nIlość zadań, których termin minął wynosi: " + overdue;
            }
            sendNotification("Lista zadań - Ilość zadań do wykonania: " + all, msg);
        }
    }

    public Boolean checkOverdueJob(String endDataTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
        String nowAsISO = df.format(new Date());
        Date now = new Date(), then = new Date();
        try {
            then = df.parse(endDataTime);
            now = df.parse(nowAsISO);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (then.before(now))
            return true;
        return false;
    }
}
