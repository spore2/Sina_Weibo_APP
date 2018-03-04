package scse.sinaweibotest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by HP233 on 2017/10/14.
 */

public class ContentAndComment extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_comment);

        Button likeButton = (Button)findViewById(R.id.like);
        Button commentButton = (Button)findViewById(R.id.comment);
        Button forwardButton = (Button)findViewById(R.id.forward);
        Button shareButton = (Button)findViewById(R.id.share);

        likeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

            }
        });

        commentButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

            }
        });

        shareButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

            }
        });

        ListView comments = (ListView)findViewById(R.id.commentList);
        //comments.setAdapter(new ArrayAdapter<>());

    }
}
