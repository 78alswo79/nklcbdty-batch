package com.nklcbdty.batch.nklcbdty.batch.email.run;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.nklcbdty.batch.nklcbdty.batch.email.dto.EmailSendRequest;

@Component
@StepScope
public class EmailContentProcessor implements ItemProcessor<Map.Entry<String, String>, EmailSendRequest> {

    @Override
    public EmailSendRequest process(Map.Entry<String, String> item) throws Exception {
        String userEmail = item.getKey();
        String content = item.getValue();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        final String title = "[네카라쿠배] " + today.format(formatter) + " 맞춤 채용 공고가 도착했어요!";

        EmailSendRequest request = new EmailSendRequest();
        request.setTo(userEmail);
        request.setSubject(title);
        request.setBody(content);

        System.out.println("Processor: '" + userEmail + "'에게 보낼 이메일 준비 완료.");
        return request;

    }
}
