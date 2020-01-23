package ng.riby.androidtest;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import ng.riby.androidtest.room.model.Note;
import ng.riby.androidtest.room.repository.NoteRepository;
import ng.riby.androidtest.utils.PH;

public class Result extends AppCompatActivity {

    @BindView(R.id.recycle)
    RecyclerView recycle;
    private NotesListAdapter notesListAdapter;
    private NoteRepository noteRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);
        PH.get().setBoolean(this,"Active",false);
        noteRepository = new NoteRepository(getApplicationContext());


        recycle.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        updateView();
    }

    private void updateView() {
        noteRepository.getTasks().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                recycle.setVisibility(View.VISIBLE);
                if (notesListAdapter == null) {
                    notesListAdapter = new NotesListAdapter(notes);
                    recycle.setAdapter(notesListAdapter);

                } else notesListAdapter.addTasks(notes);
            }
        });
    }


}
