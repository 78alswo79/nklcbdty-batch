package com.nklcbdty.batch.nklcbdty.batch.email.run;

import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.nklcbdty.batch.nklcbdty.batch.email.dto.EmailSendRequest;
import com.nklcbdty.common.email.JobEmailContentBuilder;

@Component
@StepScope
public class EmailContentProcessor implements ItemProcessor<Map.Entry<String, String>, EmailSendRequest> {

    @Override
    public EmailSendRequest process(Map.Entry<String, String> item) throws Exception {
        String userEmail = item.getKey();
        String content = item.getValue();

        EmailSendRequest request = new EmailSendRequest();
        request.setTo(userEmail);
        request.setSubject(JobEmailContentBuilder.buildDailyTitle());
        request.setBody(content);

        System.out.println("Processor: '" + userEmail + "'에게 보낼 이메일 준비 완료.");
        return request;
    }
}
