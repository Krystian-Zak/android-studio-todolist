package pl.krystianzak.to_do_list;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


@Entity(tableName = "job_table")
public class JobData implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "jobName")
    private String name;
    @ColumnInfo(name = "jobNote")
    private String note;
    @ColumnInfo(name = "jobPriority")
    private Integer priority;
    @ColumnInfo(name = "jobEndDateTime")
    private String endDateTime;
    @ColumnInfo(name = "jobStartDateTime")
    private String startDateTime;
    @ColumnInfo(name = "jobStatus")
    private Integer status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public Calendar getEndDate() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        String iso8601string = endDateTime;
        Date date = new SimpleDateFormat("yyyy-MM-dd_HH:mm").parse(iso8601string);

        calendar.setTime(date);
        return calendar;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
