package com.sourav.hacknovation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MembersVPAdapter extends FragmentPagerAdapter {

    public MembersVPAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return switch (position) {
            case 1 -> new TechnicalFragment();
            case 2 -> new LogisticFragment();
            default -> new ManagmentFragment();
        };
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Management";
            case 1:
                return "Technical";
            case 2:
                return "Media";
            default:
                return "";
        }
    }
}
