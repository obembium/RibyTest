package ng.riby.androidtest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ng.riby.androidtest.room.model.Note;
import ng.riby.androidtest.utils.AppUtils;

public class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.CustomViewHolder> {

    private List<Note> notes;
    public NotesListAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_item, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Note note = getItem(position);

        holder.itemClient.setText(note.getClientName());
        holder.itemLatitude.setText(note.getLatitude()+"");
        holder.itemLongitude.setText(note.getLongitude()+"");
        holder.itemIsOnline.setText(note.isOnline()+"");
        holder.itemTime.setText(AppUtils.getFormattedDateString(note.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public Note getItem(int position) {
        return notes.get(position);
    }

    public void addTasks(List<Note> newNotes) {
        notes.clear();
        notes.addAll(newNotes);
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView itemClient, itemLongitude, itemLatitude, itemIsOnline, itemTime;
        public CustomViewHolder(View itemView) {
            super(itemView);

            itemClient = itemView.findViewById(R.id.clientName);
            itemLongitude = itemView.findViewById(R.id.longitude);
            itemLatitude = itemView.findViewById(R.id.latitude);
            itemIsOnline = itemView.findViewById(R.id.online);
            itemTime = itemView.findViewById(R.id.time);
        }
    }
}
