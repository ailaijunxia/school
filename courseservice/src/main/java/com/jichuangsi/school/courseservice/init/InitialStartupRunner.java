package com.jichuangsi.school.courseservice.init;

import com.jichuangsi.school.courseservice.service.IElementInfoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InitialStartupRunner implements CommandLineRunner {

    @Resource
    private IElementInfoService elementInfoService;

    @Override
    public void run(String... args) throws Exception {
        elementInfoService.initQuestionType();
    }

}