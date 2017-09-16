package test;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by MyPC on 2016/12/14.
 */

public class FixTest {
    private int a=5;
    private int b=0;
    public void showToast(Context context){
        Toast.makeText(context,"a/b="+(a/b),Toast.LENGTH_SHORT).show();
    }

}
