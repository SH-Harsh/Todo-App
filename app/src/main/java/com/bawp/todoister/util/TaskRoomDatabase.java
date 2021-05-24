package com.bawp.todoister.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.bawp.todoister.data.TaskDao;
import com.bawp.todoister.model.Task;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Task.class} , version = 1 , exportSchema = false)
@TypeConverters({Converter.class})
public abstract class TaskRoomDatabase extends RoomDatabase {

    public static final int NUMBER_OF_THREADS = 4;
    private static volatile TaskRoomDatabase INSTANCE;
    public static final String DATABASE_NAME = "todoister_database";
    public static final ExecutorService databaseWriterExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static final RoomDatabase.Callback sRoomDatabaseCallBack =
            new RoomDatabase.Callback(){
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    databaseWriterExecutor.execute( () -> {

                        //invoke dao and write
                        TaskDao taskDao = INSTANCE.taskDao();
                        taskDao.deleteAll();      //clean state
                    });

                }
            };

    public static TaskRoomDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (TaskRoomDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TaskRoomDatabase.class,DATABASE_NAME)
                            .addCallback(sRoomDatabaseCallBack)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract TaskDao taskDao();
}