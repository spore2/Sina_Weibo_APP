package scse.sinaweibotest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by HP233 on 2017/10/17.
 */

public class LeftDrawerFragment extends Fragment {

    private Activity mActivity;
    private ListView mDrawList;
    private LinearLayout mLinearLayout;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.left_drawer, container, false);

        mDrawList = view.findViewById(R.id.listContent);
        mLinearLayout = view.findViewById(R.id.drawerContent);
        mDrawerLayout = ((MainActivity)mActivity).getDrawerLayout();
        initDraw();

        return view;
    }

    private void initDraw(){

        mDrawList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TestFragment temp;

                if((temp = (TestFragment)((MainActivity)mActivity).getFragments()[1]) != null){
                    switch(i){
                        case 0: temp.changeContent("Account"); break;
                        case 1: temp.changeContent("Favorite"); break;
                        case 2: temp.changeContent("Draft"); break;
                        default: temp.changeContent("Default");
                    }
                }
                mDrawerLayout.closeDrawer(mLinearLayout);
            }
        });

    }
}
