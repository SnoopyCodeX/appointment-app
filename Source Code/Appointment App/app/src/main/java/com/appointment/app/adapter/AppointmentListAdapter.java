package com.appointment.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.appointment.app.R;
import com.appointment.app.adapter.holder.AppointmentListHolder;
import com.appointment.app.adapter.holder.BaseListHolder;
import com.appointment.app.model.AppointmentModel;

import java.util.List;

public class AppointmentListAdapter extends RecyclerView.Adapter<BaseListHolder>
{
    private List<AppointmentModel> items;
    private AppCompatActivity activity;

    public AppointmentListAdapter(AppCompatActivity activity, List<AppointmentModel> items)
    {
        this.items = items;
        this.activity = activity;
    }

    @NonNull
    @Override
    public BaseListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppointmentListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_appointment_item, parent, false), items, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseListHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }

    public void addAppointments(List<AppointmentModel> items)
    {
        if(this.items != null)
            this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void removeAppointment(int position)
    {
        if(items != null && items.size() > 0 && position < items.size())
            this.items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, items.size());
    }

    public void addAppointment(AppointmentModel appointment)
    {
        if(items != null)
            items.add(0, appointment);
        notifyItemInserted(0);
        notifyItemRangeInserted(0, items.size());
    }

    public void updateAppointment(AppointmentModel appointment, int position)
    {
        if(items != null && items.size() > 0 && position < items.size())
        {
            this.items.add(position, appointment);
            notifyItemChanged(position);

            moveAppointmentToTop(appointment);
        }
    }

    public void moveAppointmentToTop(AppointmentModel appointment)
    {
        if(items != null && items.size() > 0)
        {
            int fromIndex = items.indexOf(appointment);
            this.items.remove(fromIndex);
            this.items.add(0, appointment);
            notifyItemMoved(fromIndex, 0);
        }
    }

    public void clearList()
    {
        if(this.items != null)
            this.items.clear();
        notifyDataSetChanged();
    }

    public AppointmentModel getAppointment(int position)
    {
        if(this.items != null)
            return items.get(position);
        return null;
    }
}
