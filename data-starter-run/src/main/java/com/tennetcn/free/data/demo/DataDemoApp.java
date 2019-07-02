package com.tennetcn.free.data.demo;

import com.tennetcn.free.data.demo.logical.model.LoginUser;
import com.tennetcn.free.data.demo.logical.service.ILoginUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class DataDemoApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DataDemoApp.class, args);
    }

    @Autowired
    private ILoginUserService loginUserService;

    @Override
    public void run(String... args) throws Exception {
        int count = loginUserService.queryCount();

        System.out.println("count:" + count);

        List<LoginUser> loginUsers = loginUserService.queryList();

//        loginUsers.forEach(user->{
//            System.out.println(user.getName());
//        });
    }
}