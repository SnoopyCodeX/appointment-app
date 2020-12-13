package com.appointment.app.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.appointment.app.fragment.DoctorApprovedFragment;
import com.appointment.app.fragment.DoctorPendingFragment;

@SuppressWarnings("ALL")
public class SectionsPagerAdapter extends FragmentPagerAdapter
{
    private static final String[] TAB_TITLES = {"Approved Requests", "Pending Requests"};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch(position + 1)
        {
            case 1:
                return DoctorApprovedFragment.newInstance();

            case 2:
                return DoctorPendingFragment.newInstance();
        }

        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        return TAB_TITLES[position];
    }

    @Override
    public int getCount()
    {
        return TAB_TITLES.length;
    }
}