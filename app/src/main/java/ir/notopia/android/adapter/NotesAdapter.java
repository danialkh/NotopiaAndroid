package ir.notopia.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ir.notopia.android.EditorActivity;
import ir.notopia.android.R;
import ir.notopia.android.database.entity.NoteEntity;
import ir.notopia.android.utils.Constants;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    private final List<NoteEntity> mNotes;
    private final Context mContext;

    public NotesAdapter(List<NoteEntity> mNotes, Context mContext) {
        this.mNotes = mNotes;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_notes, parent, false);
        return new NotesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final NoteEntity note = mNotes.get(position);
        switch (note.getNoteType()) {
            case Constants.NOTE_TYPE_AUDIO:
                holder.mImageView.setImageResource(R.drawable.ic_audio);
                break;
            case Constants.NOTE_TYPE_IMAGE:
                holder.mImageView.setImageResource(R.drawable.ic_image);
                break;
            case Constants.NOTE_TYPE_VIDEO:
                holder.mImageView.setImageResource(R.drawable.ic_video);
                break;
            case Constants.NOTE_TYPE_TEXT :
                holder.mImageView.setImageResource(R.drawable.ic_note);
                break;
            case Constants.NOTE_TYPE_VOICE_TEXT:
                holder.mImageView.setImageResource(R.drawable.ic_note);
                break;
        }
        holder.mNoteContent.setText(note.getDescription());
        holder.itemView.setOnClickListener(v -> {
            Intent intentEditor = new Intent(mContext, EditorActivity.class);
            int noteId = note.getNoteId();
            intentEditor.putExtra(Constants.NOTE_ID_EXTRA, noteId);

            mContext.startActivity(intentEditor);
        });
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNoteContent;
        ImageView mImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mNoteContent = itemView.findViewById(R.id.tV_note_content);
            mImageView = itemView.findViewById(R.id.IVNextMonthFilterCalender);
            itemView.setTag(this);

        }
    }
}
