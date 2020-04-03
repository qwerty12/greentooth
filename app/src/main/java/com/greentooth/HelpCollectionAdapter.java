package com.greentooth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class HelpCollectionAdapter extends FragmentStateAdapter {
    String[] helpStrings;

    public HelpCollectionAdapter(Fragment fragment) {
        super(fragment);
        helpStrings = fragment.getResources().getStringArray(R.array.help_entries);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        Fragment fragment = new HelpObjectFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putStringArray("texts", helpStrings);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        if (null == helpStrings) {
            return 0;
        } else {
            return helpStrings.length;
        }
    }

}