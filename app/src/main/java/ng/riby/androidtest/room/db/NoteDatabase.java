package ng.riby.androidtest.room.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import ng.riby.androidtest.room.dao.DaoAccess;
import ng.riby.androidtest.room.model.Note;


@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    public abstract DaoAccess daoAccess();
}
