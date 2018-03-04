package scse.sinaweibotest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by HP233 on 2017/10/17.
 */

public class TestFragment extends Fragment {

    //private TextView text;
    private View.OnClickListener onClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        //text = view.findViewById(R.id.text);
        /*onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SendWeiboActivity.class);
                startActivity(intent);
            }
        };*/
        return view;
    }

    /*@Override
    public void onResume(){
        super.onResume();
        //FloatingActionButton fab = getActivity().findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SendWeiboActivity.class);
                startActivity(intent);
            }
        });
    }*/

    public void changeContent(String content) {
        /*if (text != null) {
            text.setText(content);
        }*/
    }
}
