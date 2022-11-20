package pl.krystianzak.to_do_list.ui;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.Date;
import java.util.List;

import pl.krystianzak.to_do_list.JobData;

@Dao
public interface MainDao {
    //Insert query
    @Insert(onConflict = REPLACE)
    void insert(JobData jobData);

    //Delete query
    @Delete
    void delete(JobData jobData);

    //Delete all query
    @Delete
    void reset(List<JobData> jobData);

    //Update query
    @Query("Update job_table SET jobName = :sName , jobNote = :sJobNote , jobPriority = :sJobPriority, jobEndDateTime = :sEndDateTime, jobStatus = :sJobStatus WHERE ID = :sID")
    void update(int sID, String sName, String sJobNote, Integer sJobPriority, String sEndDateTime, Integer sJobStatus);

    //Update all status query
    @Query("Update job_table SET  jobStatus = :sJobStatus")
    void updateStatusAll(Integer sJobStatus);

    //Get all data query
    @Query("SELECT * FROM job_table")
    List<JobData> getAll();

    //Get all done jobs data query
    @Query("SELECT * FROM job_table WHERE jobStatus = 0")
    List<JobData> getAllProgress();

    //Get jobs by rawquery
    @RawQuery
    List<JobData> getJobsRaw(SupportSQLiteQuery query);

    @RawQuery
    int checkpoint(SupportSQLiteQuery supportSQLiteQuery);
}
