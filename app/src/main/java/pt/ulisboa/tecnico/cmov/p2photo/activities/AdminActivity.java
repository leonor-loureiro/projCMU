package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import pt.ulisboa.tecnico.cmov.p2photo.R;

public class AdminActivity extends AppCompatActivity {

    private PageAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new PageAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText("Server Log"));
        tabLayout.addTab(tabLayout.newTab().setText("App Log"));
        tabLayout.addTab(tabLayout.newTab().setText("Storage Setting"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorAccent));

        vpPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));



    }

    public static class PageAdapter extends FragmentPagerAdapter {

        private final int NUM_PAGES = 3;

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new ServerLogFragment();
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return new ServerLogFragment();
                case 2: // Fragment # 1 - This will show SecondFragment
                    return new CacheSettingsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }
}
