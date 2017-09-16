package com.emery.test.tinker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import FiexUtils.FixUtil;
import test.FixTest;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void test(View view) {
        new FixTest().showToast(this);
    }

    public void fix(View view) {
        Toast.makeText(this,"点击了修复",Toast.LENGTH_SHORT).show();
          fixBug();
    }

    public void fixBug() {
        //检索application文件夹（存放修复的dex) /data/data/packageName/odex
        File fileDir = getDir(MyConstants.DEX_DIR, Context.MODE_PRIVATE);
        //被修复好的文件打成的dex文件名
        String fixedDexName = "classes2.dex";
        //该目录放置我们修复好的dex文件    /data/data/packageName/odex/classes3.dex
        String fixedDexPath = fileDir.getAbsolutePath() + File.separator + fixedDexName;
        //删除原来的已经存在该目录下同名的dex文件
        File fixedDexFile = new File(fixedDexPath);
        if (fixedDexFile.exists()) {
            fixedDexFile.delete();
        }
        //从sd卡把classes3.dex复制到application目录下
        try {
            String targetFile = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + fixedDexName;
            FileInputStream fileReader = new FileInputStream(targetFile);
            FileOutputStream fileWriter = new FileOutputStream(fixedDexPath);
            byte[] buf = new byte[1024 * 10];
            int len = 0;

            while ((len = fileReader.read(buf)) != -1) {
                fileWriter.write(buf, 0, len);
                fileWriter.flush();
            }
            if (fixedDexFile.exists() && fixedDexFile.length() == new File(targetFile).length()){
                Toast.makeText(this,"新的dex文件复制到application目录成功",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"新的dex文件复制到application目录失败",Toast.LENGTH_SHORT).show();
            }
                fileReader.close();
                fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FixUtil.loadDexFile(getApplicationContext());
    }
}
