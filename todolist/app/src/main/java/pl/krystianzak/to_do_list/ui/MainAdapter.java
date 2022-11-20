package pl.krystianzak.to_do_list.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.text.BoringLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import kotlinx.coroutines.Job;
import pl.krystianzak.to_do_list.JobData;
import pl.krystianzak.to_do_list.MainActivity;
import pl.krystianzak.to_do_list.R;

public class MainAdapter extends RecyclerView.Adapter<ViewHolder> {

    //Initialize variable
    private List<JobData> dataList;
    private Activity context;
    private RoomDB database;

    private List<JobData> dataListCopy;

    int pickerHour, pickerMinute, priority;

    //Constructor
    public MainAdapter(Activity context, List<JobData> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.dataListCopy = new ArrayList<>(dataList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Initialize view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_main, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Initialize main data
        JobData data = dataList.get(position);
        //Initialize database
        database = RoomDB.getInstance(context);
        //Set text on text view
        holder.name.setText(data.getName());
        holder.checkBoxJobDone.setChecked(intToBool(data.getStatus()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm"); // Quoted "Z" to indicate UTC, no timezone offset
        String nowAsISO = df.format(new Date());
        Date now = new Date(), then = new Date();
        try {
            then = df.parse(data.getEndDateTime());
            now = df.parse(nowAsISO);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (then.before(now)) {
            holder.endDateTime.setTextColor(context.getResources().getColor(R.color.red));
        } else {
            holder.endDateTime.setTextColor(context.getResources().getColor(R.color.jobCardViewText));
        }

        try {
            holder.endDateTime.setText(context.getResources().getString(R.string.deadline) + ": " + dateFormat.format(data.getEndDate().getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (data.getPriority()) {
            case 0:
                holder.blockPriority.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                break;

            case 1:
                holder.blockPriority.setBackgroundColor(ContextCompat.getColor(context, R.color.orange));
                break;

            case 2:
                holder.blockPriority.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                break;
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                final Dialog newJobDialog = new Dialog(context);
                newJobDialog.setTitle("Zadanie");


                newJobDialog.setContentView(R.layout.dialog_print_job);

                final Button btnOk = newJobDialog.findViewById(R.id.buttonPrintJobOk);
                final TextView tvName = newJobDialog.findViewById(R.id.textViewPrintJobName);
                final TextView tvNote = newJobDialog.findViewById(R.id.textViewPrintJobNote);
                final TextView tvStartDate = newJobDialog.findViewById(R.id.textViewPrintJobStartDate);
                final TextView tvEndDate = newJobDialog.findViewById(R.id.textViewPrintJobEndDate);
                final TextView tvStatus = newJobDialog.findViewById(R.id.textViewPrintJobStatus);
                final TextView tvPriority = newJobDialog.findViewById(R.id.textViewPrintJobPrioritet);
                final Button btnJobExportTxt = newJobDialog.findViewById(R.id.buttonJobExportText);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm"); // Quoted "Z" to indicate UTC, no timezone offset
                String nowAsISO = df.format(new Date());
                Date now = new Date(), then = new Date();
                try {
                    then = df.parse(data.getEndDateTime());
                    now = df.parse(nowAsISO);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (then.before(now)) {
                    tvEndDate.setTextColor(context.getResources().getColor(R.color.red));
                } else {
                    tvEndDate.setTextColor(context.getResources().getColor(R.color.jobCardViewText));
                }

                tvEndDate.setText(data.getEndDateTime().replace("_", " ") + "");
                tvStartDate.setText(data.getStartDateTime().replace("_", " ") + "");
                tvName.setText(data.getName());
                tvNote.setText(data.getNote());
                tvStatus.setText((data.getStatus() == 0 ? context.getResources().getString(R.string.dialog_job_status0) : context.getResources().getString(R.string.dialog_job_status1)) + "");
                if (data.getStatus() == 0)
                    tvStatus.setTextColor(Color.parseColor("#FFFF0000"));
                else
                    tvStatus.setTextColor(Color.parseColor("#FF00FF00"));


                switch (data.getPriority()) {
                    case 0:
                        tvPriority.setText(context.getResources().getString(R.string.priority_easy));
                        tvPriority.setTextColor(Color.parseColor("#FF4CAF50"));
                        break;
                    case 1:
                        tvPriority.setText(context.getResources().getString(R.string.priority_medium));
                        tvPriority.setTextColor(Color.parseColor("#FFFF9800"));
                        break;
                    case 2:
                        tvPriority.setText(context.getResources().getString(R.string.priority_hard));
                        tvPriority.setTextColor(Color.parseColor("#FFF44336"));
                        break;
                }

                newJobDialog.findViewById(R.id.buttonPrintJobOk).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newJobDialog.dismiss();
                    }
                });

                newJobDialog.show();
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(newJobDialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                //layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                newJobDialog.getWindow().setAttributes(layoutParams);


                btnJobExportTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setTitle(context.getResources().getString(R.string.dialog_export_txt_title))
                                .setMessage(context.getResources().getString(R.string.dialog_export_txt_question) + ": /" + context.getResources().getString(R.string.app_name) + "/" + data.getName() + ".txt ?" + context.getResources().getString(R.string.dialog_export_txt_question2))
                                .setIcon(R.drawable.ic_baseline_info)
                                .setPositiveButton(context.getResources().getString(R.string.yes),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (writeReadPerm()) {
                                                    String appName = context.getResources().getString(R.string.app_name);
                                                    File file = new File(Environment.getExternalStorageDirectory() + "/" + appName + "/");
                                                    if (!file.exists()) {
                                                        file.mkdirs();
                                                    }

                                                    File sd = Environment.getExternalStorageDirectory();

                                                    if (sd.canWrite()) {

                                                        file = new File(Environment.getExternalStorageDirectory() + "/" + appName + "/" + data.getName() + ".txt");
                                                        try {
                                                            file.createNewFile();
                                                            FileOutputStream fOut = new FileOutputStream(file);
                                                            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                                            String dataTxt = context.getResources().getString(R.string.dialog_job_name) + "\n";
                                                            dataTxt += data.getName() + "\n\n";

                                                            dataTxt += context.getResources().getString(R.string.dialog_job_notes) + "\n";
                                                            dataTxt += data.getNote() + "\n\n";

                                                            dataTxt += context.getResources().getString(R.string.dialog_job_dateStart) + "\n";
                                                            dataTxt += data.getStartDateTime() + "\n\n";

                                                            dataTxt += context.getResources().getString(R.string.dialog_job_date) + "\n";
                                                            dataTxt += data.getEndDateTime() + "\n\n";

                                                            String status = "", priorytet = "";

                                                            if (data.getPriority() == 0)
                                                                priorytet = context.getResources().getString(R.string.priority_easy);
                                                            else if (data.getPriority() == 1)
                                                                priorytet = context.getResources().getString(R.string.priority_medium);
                                                            else if (data.getPriority() == 2)
                                                                priorytet = context.getResources().getString(R.string.priority_hard);

                                                            if (data.getStatus() == 0)
                                                                status = context.getResources().getString(R.string.dialog_job_status0);
                                                            else
                                                                status = context.getResources().getString(R.string.dialog_job_status1);

                                                            dataTxt += context.getResources().getString(R.string.dialog_job_priority) + "\n";
                                                            dataTxt += priorytet + "\n\n";

                                                            dataTxt += context.getResources().getString(R.string.dialog_job_status) + "\n";
                                                            dataTxt += status + "\n\n";

                                                            myOutWriter.append(dataTxt);

                                                            myOutWriter.close();

                                                            fOut.flush();
                                                            fOut.close();
                                                            Toast.makeText(context, context.getResources().getString(R.string.export_succes), Toast.LENGTH_LONG).show();

                                                        } catch (IOException e) {
                                                            Toast.makeText(context, context.getResources().getString(R.string.settings_exportError), Toast.LENGTH_LONG).show();
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        Toast.makeText(context, context.getResources().getString(R.string.noStoragePermission), Toast.LENGTH_LONG).show();
                                                    }
                                                } else {
                                                    requestWriteReadPerm();

                                                }
                                            }
                                        })
                                .setNegativeButton(context.getResources().getString(R.string.no), null)
                                .create()
                                .show();


                    }
                });
            }
        });

        holder.imageViewJobDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.delete_title))
                        .setMessage(context.getResources().getString(R.string.delete_question))
                        .setIcon(R.drawable.ic_baseline_delete_24)
                        .setPositiveButton(context.getResources().getString(R.string.yes),
                                (dialog, which) -> {
                                    //Initialize main data
                                    JobData d = dataList.get(holder.getAdapterPosition());
                                    //Delete text from database
                                    database.mainDao().delete(d);
                                    //Notify when data is deleted
                                    int position2 = holder.getAdapterPosition();
                                    dataList.remove(position2);
                                    dataListCopy.remove(position2);
                                    notifyItemRemoved(position2);
                                    notifyItemRangeChanged(position2, dataList.size());
                                })
                        .setNegativeButton(context.getResources().getString(R.string.no), null)
                        .create()
                        .show();
            }
        });

        holder.imageViewJobEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog newJobDialog = new Dialog(context);
                newJobDialog.setTitle("Edytuj zadanie");

                newJobDialog.setContentView(R.layout.dialog_new_job);

                final RadioButton newJobPriorityEasy = newJobDialog.findViewById(R.id.radioButtonEasy);
                final RadioButton newJobPriorityMedium = newJobDialog.findViewById(R.id.radioButtonMedium);
                final RadioButton newJobPriorityHard = newJobDialog.findViewById(R.id.radioButtonHard);
                final Button btnNewJobSelectEndDate = newJobDialog.findViewById(R.id.buttonNewJobSelectEndDate);
                final Button btnNewJobSelectTime = newJobDialog.findViewById(R.id.buttonNewJobSelectTime);
                ((Button) newJobDialog.findViewById(R.id.buttonNewJobAdd)).setText(context.getResources().getString(R.string.update));
                ((EditText) newJobDialog.findViewById(R.id.editTextNewJobName)).setText(data.getName());
                ((EditText) newJobDialog.findViewById(R.id.editTextNewJobDescription)).setText(data.getNote());


                Calendar myCalendar = Calendar.getInstance();
                try {
                    myCalendar = data.getEndDate();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar finalMyCalendar = myCalendar;
                pickerHour = myCalendar.get(Calendar.HOUR_OF_DAY);
                pickerMinute = myCalendar.get(Calendar.MINUTE);
                btnNewJobSelectTime.setText(String.format(Locale.getDefault(), "%02d:%02d", pickerHour, pickerMinute));

                String myFormat = "dd/MM/YYYY";
                SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat);
                btnNewJobSelectEndDate.setText(dateFormat.format(finalMyCalendar.getTime()));
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        finalMyCalendar.set(Calendar.YEAR, year);
                        finalMyCalendar.set(Calendar.MONTH, month);
                        finalMyCalendar.set(Calendar.DAY_OF_MONTH, day);
                        btnNewJobSelectEndDate.setText(dateFormat.format(finalMyCalendar.getTime()));

                    }
                };

                btnNewJobSelectEndDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DatePickerDialog(context, date, finalMyCalendar.get(Calendar.YEAR), finalMyCalendar.get(Calendar.MONTH), finalMyCalendar.get(Calendar.DAY_OF_MONTH)).show();
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
                        timePickerDialog.setTitle(context.getResources().getString(R.string.dialog_job_time_select));
                        timePickerDialog.show();

                    }
                });


                newJobDialog.findViewById(R.id.buttonNewJobCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newJobDialog.dismiss();
                    }
                });

                newJobDialog.show();
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(newJobDialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                //layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                newJobDialog.getWindow().setAttributes(layoutParams);

                switch (data.getPriority()) {
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

                newJobDialog.findViewById(R.id.buttonNewJobAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getApplicationContext(), "Nowe zadanie: " + newJobName.getText().toString(), Toast.LENGTH_SHORT).show();
                        String name = ((EditText) newJobDialog.findViewById(R.id.editTextNewJobName)).getText().toString().trim();
                        String notes = ((EditText) newJobDialog.findViewById(R.id.editTextNewJobDescription)).getText().toString().trim();


                        String myFormat = "yyyy-MM-dd";
                        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat);
                        Boolean er = false;
                        String msg = "";
                        if (TextUtils.isEmpty(name)) {
                            msg = context.getResources().getString(R.string.nameIsEmpty) + '\n';
                            er = true;
                        } else if (name.length() > 40) {
                            msg = context.getResources().getString(R.string.nameIsTooLong) + '\n';
                            er = true;
                        }
                        String endDateAsIso = "";
                        if (finalMyCalendar.getTime() != null && !TextUtils.isEmpty(Integer.toString(pickerHour)) && !TextUtils.isEmpty(Integer.toString(pickerMinute))) {
                            endDateAsIso = dateFormat.format(finalMyCalendar.getTime()) + "_" + String.format(Locale.getDefault(), "%02d:%02d", pickerHour, pickerMinute);
                        } else {
                            msg = context.getResources().getString(R.string.endDateTimeIsEmpty) + '\n';
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
                            data.setName(name);
                            data.setNote(TextUtils.isEmpty(notes) ? "" : notes);

                            data.setEndDateTime(endDateAsIso);

                            data.setPriority(priority);

                            //Insert text in database
                            database.mainDao().update(data.getId(), name, notes, priority, endDateAsIso, data.getStatus());

                            dataList.get(holder.getAdapterPosition()).setName(name);
                            dataList.get(holder.getAdapterPosition()).setNote(notes);
                            dataList.get(holder.getAdapterPosition()).setEndDateTime(endDateAsIso);
                            dataList.get(holder.getAdapterPosition()).setPriority(priority);

                            dataListCopy.get(holder.getAdapterPosition()).setName(name);
                            dataListCopy.get(holder.getAdapterPosition()).setNote(notes);
                            dataListCopy.get(holder.getAdapterPosition()).setEndDateTime(endDateAsIso);
                            dataListCopy.get(holder.getAdapterPosition()).setPriority(priority);

                            notifyDataSetChanged();
                            Toast.makeText(context, context.getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                            newJobDialog.dismiss();
                        } else {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        /*holder.checkBoxJobDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                database.mainDao().update(data.getId(), data.getName(), data.getNote(), data.getPriority(), data.getEndDateTime(), isBooleanTrue(isChecked));
                Toast.makeText(context,isChecked+" id: "+data.getId(),Toast.LENGTH_SHORT).show();
                //notifyDataSetChanged();
            }
        });*/

        holder.checkBoxJobDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.mainDao().update(data.getId(), data.getName(), data.getNote(), data.getPriority(), data.getEndDateTime(), boolToInt(holder.checkBoxJobDone.isChecked()));
                dataList.get(holder.getAdapterPosition()).setStatus(boolToInt(holder.checkBoxJobDone.isChecked()));
                dataListCopy.get(holder.getAdapterPosition()).setStatus(boolToInt(holder.checkBoxJobDone.isChecked()));
                //Toast.makeText(context,holder.checkBoxJobDone.isChecked()+" id: "+data.getId(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Integer boolToInt(Boolean b) {
        return b == true ? 1 : 0;
    }

    public Boolean intToBool(int b) {
        return b >= 1 ? true : false;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void filter(String text) {

        dataList.clear();
        if (TextUtils.isEmpty(text)) {
            dataList.addAll(dataListCopy);
        } else {
            text = text.toLowerCase();
            for (JobData item : dataListCopy) {
                if (item.getName().toLowerCase().contains(text)) {
                    dataList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void copyDataList() {
        dataListCopy = new ArrayList<>(dataList);
    }

    public void requestWriteReadPerm() {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.settings_importExportPermissionTitle))
                .setMessage(context.getString(R.string.settings_importExportPermission))
                .setIcon(R.drawable.ic_baseline_info)
                .setPositiveButton(context.getString(R.string.yes),
                        (dialog, which) -> {
                            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .create()
                .show();
    }

    public boolean writeReadPerm() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }
}
