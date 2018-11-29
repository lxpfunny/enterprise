package com.lxpfunny.enterprise.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author lixiaopeng
 * @date 2018/11/27 15:50
 */
@RestController
@RequestMapping("/pk101")
public class ThreadController {
    List<ExecutorService> services = new ArrayList<>();
    @GetMapping("/start")
    public void start(@RequestParam("cookie")String cookie) {
        if(services.size() > 0){
            throw new RuntimeException("下单程序正在运行，先终止程序，在下单");
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        services.add(executor);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    System.out.println("ffff:"+Thread.currentThread().getName());
                    Thread.sleep(30000);
                    System.out.println("ssss:"+Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    System.out.println("zzz:"+Thread.currentThread().getName());
                    System.out.println("线程中断异常");
                    Thread.interrupted();
                    e.printStackTrace();
                }
            }
        });

    }

    @GetMapping("/stop")
    public void stop() {
        for (ExecutorService service : services) {
            service.shutdownNow();
        }
        services.clear();
    }
}
