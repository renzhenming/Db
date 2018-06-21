package com.rzm.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.rzm.commonlibrary.general.sqlite.DaoFactory;
import com.rzm.commonlibrary.general.sqlite.IDao;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IDao<User> dao = DaoFactory.getInstance().getDataHelper(UserDao.class, User.class);

    }
}
