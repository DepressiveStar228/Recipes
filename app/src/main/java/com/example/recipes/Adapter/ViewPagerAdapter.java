package com.example.recipes.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipes.Fragments.CollectionsDishFragment;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private List<Fragment> fragmentList;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragments) {
        super(fragmentActivity);
        this.fragmentList = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public void updateCollectionFragment() {
        ((CollectionsDishFragment) fragmentList.get(2)).updateCounterDishes();
        ((CollectionsDishFragment) fragmentList.get(2)).updateCollections();
    }

    public List<Fragment> getFragmentList() {
        return fragmentList;
    }
}
