package com.rzm.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.rzm.commonlibrary.general.sqlite.DaoFactory;
import com.rzm.commonlibrary.general.sqlite.IDao;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private IDao<User> dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dao = DaoFactory.getInstance().getDataHelper(UserDao.class, User.class);

    }

    public void insert(View view){
        for (int i = 0; i < 10; i++) {

            User user = new User();
            user.name = "张三"+i;
            user.password = "123456"+i;
            dao.insert(user);
        }
        Toast.makeText(getApplicationContext(),"插入成功",Toast.LENGTH_SHORT).show();
    }
    public void delete(View view){
        User user = new User();
        user.name = "张三5";
        Long delete = dao.delete(user);
        Toast.makeText(getApplicationContext(),"删除"+delete+"条数据",Toast.LENGTH_SHORT).show();
    }
    public void update(View view) {
        User userOld = new User();
        userOld.name = "张三1";

        User userNew = new User();
        userNew.name = "里斯";
        Long update = dao.update(userNew, userOld);
        Toast.makeText(getApplicationContext(),"更新"+update+"条数据",Toast.LENGTH_SHORT).show();
    }

    public void query(View view) {
        User userOld = new User();
        userOld.name = "张三2";
        List<User> query = dao.query(userOld);
        for (int i = 0; i < query.size(); i++) {
            Log.d("tag",query.get(i).name+","+query.get(i).password);
        }
    }
}
