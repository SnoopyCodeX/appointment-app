package com.appointment.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.appointment.app.R;
import com.appointment.app.adapter.holder.BaseListHolder;
import com.appointment.app.adapter.holder.DoctorListHolder;
import com.appointment.app.model.DoctorModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DoctorListAdapter extends RecyclerView.Adapter<BaseListHolder>
{
    private AppCompatActivity activity;
    private List<DoctorModel> items;

    public DoctorListAdapter(AppCompatActivity activity, List<DoctorModel> items)
    {
        this.activity = activity;
        this.items = items;
    }

    @NonNull
    @Override
    public BaseListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new DoctorListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_doctor_item, parent, false), items, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseListHolder holder, int position)
    {
        holder.onBind(position);
    }

    @Override
    public int getItemCount()
    {
        return (items != null) ? items.size() : 0;
    }

    public void clearList()
    {
        if(this.items != null)
            this.items.clear();
        notifyDataSetChanged();
    }

    public void addDoctor(DoctorModel doctorModel)
    {
        if(items != null)
            items.add(0, doctorModel);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, items.size());
    }

    public void addAllDoctors(List<DoctorModel> doctorModels)
    {
        if(items != null)
            items.addAll(doctorModels);
        notifyItemRangeChanged(0, items == null ? 0 : items.size());
    }

    public void removeDoctor(int position)
    {
        if(items != null && items.size() > 0 && position < items.size())
            items.remove(position);
        notifyItemRemoved(position);
    }

    public void moveDoctorToTop(DoctorModel doctorModel)
    {
        if(items != null && items.size() > 0)
        {
            int fromIndex = getIndexOf(doctorModel.id);
            items.remove(fromIndex);
            items.add(0, doctorModel);
            notifyItemMoved(fromIndex, 0);
            notifyItemChanged(0);
        }
    }

    public int getIndexOf(int doctorId)
    {
        AtomicInteger index = new AtomicInteger();
        index.set(-1);

        activity.runOnUiThread(() -> {
            for(DoctorModel doctorModel : items)
                if(doctorId != doctorModel.id)
                    index.addAndGet(1);
                else
                {
                    index.addAndGet(1);
                    break;
                }
        });

        return index.get();
    }

    public DoctorModel getDoctor(int position)
    {
        if(items != null && items.size() > 0)
            return items.get(position);
        return null;
    }
}
