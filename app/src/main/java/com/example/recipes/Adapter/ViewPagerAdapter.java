package com.example.recipes.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipes.Fragments.CollectionsDishFragment;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для роботи з ViewPager, який керує відображенням фрагментів.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {
    private List<Fragment> fragmentList;

    /**
     * Конструктор адаптера.
     *
     * @param fragmentActivity Батьківська FragmentActivity, яка містить ViewPager
     * @param fragments Список фрагментів для відображення у ViewPager
     */
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragments) {
        super(fragmentActivity);
        this.fragmentList = fragments;
    }

    /**
     * Створює та повертає фрагмент для заданої позиції.
     *
     * @param position Позиція фрагмента у ViewPager
     * @return Фрагмент для відображення
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    /**
     * Повертає загальну кількість фрагментів у адаптері.
     *
     * @return Кількість фрагментів
     */
    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    /**
     * Повертає список усіх фрагментів, які містяться в адаптері.
     *
     * @return Список фрагментів
     */
    public List<Fragment> getFragmentList() {
        return fragmentList;
    }
}
