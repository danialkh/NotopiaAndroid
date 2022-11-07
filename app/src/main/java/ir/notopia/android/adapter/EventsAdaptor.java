package ir.notopia.android.adapter;

        import android.app.Activity;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
        import java.util.List;

        import androidx.recyclerview.widget.RecyclerView;

        import ir.mirrajabi.persiancalendar.core.models.CalendarEvent;
        import ir.notopia.android.R;

public class EventsAdaptor extends DragSelectRecyclerViewAdapter<EventsAdaptor.ThumbViewHolder> {

    private static final String TAG = "Events Adaptor";
    private List<CalendarEvent> mEvents;
    private Activity _activity;

    // Constructor takes click listener callback
    public EventsAdaptor(List<CalendarEvent> tempEvents, Activity activity) {
        super();
        mEvents = tempEvents;
        this._activity = activity;
    }
    @Override
    public ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_item, parent, false);
        return new ir.notopia.android.adapter.EventsAdaptor.ThumbViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ThumbViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!

        CalendarEvent event = mEvents.get(position);


        String str = event.getDate().getYear() + "/" + event.getDate().getMonth() + "/" + event.getDate().getDayOfMonth();


        holder.eventDate.setText(str);
        holder.eventTitle.setText(event.getTitle());


        Log.d(TAG,"kooool" + str);
        Log.d(TAG,"kooool" + event.getTitle());

    }



    @Override
    public int getItemCount() {
        return mEvents.size();
    }


    public class ThumbViewHolder extends RecyclerView.ViewHolder {


        public TextView eventDate;
        public TextView eventTitle;

        public ThumbViewHolder(View itemView) {
            super(itemView);
            this.eventDate = itemView.findViewById(R.id.TVEventDate);
            this.eventTitle = itemView.findViewById(R.id.TVEventTitle);

        }



    }
}
