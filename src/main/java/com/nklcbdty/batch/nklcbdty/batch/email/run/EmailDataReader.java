package com.nklcbdty.batch.nklcbdty.batch.email.run;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import com.nklcbdty.batch.nklcbdty.batch.email.service.EmailService;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class EmailDataReader implements ItemReader<Map.Entry<String, String>> {
    private final EmailService emailService;
    private Iterator<Map.Entry<String, String>> dataIterator;
    private boolean initialized = false;

    @Override
    public Map.Entry<String, String> read() throws NonTransientResourceException {
        if (!initialized) {
            initialize(); // 데이터를 한 번만 로드합니다.
            initialized = true;
        }
        if (dataIterator != null && dataIterator.hasNext()) {
            Map.Entry<String, String> item = dataIterator.next();
            System.out.println("Reader: 이메일 데이터 읽음 - " + item.getKey());
            return item;
        }
        System.out.println("Reader: 더 이상 읽을 이메일 데이터가 없습니다.");
        return null;
    }

    private void initialize() {
        Map<String, List<String>> userCategoryMap = emailService.getUserCategoryMap();
        Map<String, String> ab = emailService.sendEmail(userCategoryMap.get("AB"));
        this.dataIterator = ab.entrySet().iterator();
    }

}
