package com.appointment.app.adapter.holder;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseListHolder extends RecyclerView.ViewHolder
{
    private int currentPosition;

    public BaseListHolder(View itemView)
    {
        super(itemView);
    }

    protected abstract void clear();

    public void onBind(int position)
    {
        this.currentPosition = position;
        clear();
    }

    public int getCurrentPosition()
    {
        return currentPosition;
    }
}
