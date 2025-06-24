package com.nklcbdty.batch.nklcbdty.batch.email.run;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.nklcbdty.batch.nklcbdty.batch.email.dto.EmailSendRequest;
import com.nklcbdty.batch.nklcbdty.batch.email.service.EmailService;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class EmailSenderWriter implements ItemWriter<EmailSendRequest> {

    private final EmailService emailService;

    @Override
    public void write(Chunk<? extends EmailSendRequest> items) throws Exception {
        System.out.println("Writer: " + items.size() + "개의 이메일 발송을 시도합니다.");
        for (EmailSendRequest request : items) {
            try {
                emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
                System.out.println("Writer: '" + request.getTo() + "'에게 이메일 발송 성공!");
            } catch (Exception e) {
                // Spring Batch의 fault tolerance 기능이 이 예외를 잡아서 skip/retry를 처리합니다.
                System.err.println("Writer: '" + request.getTo() + "'에게 이메일 발송 실패 - " + e.getMessage());
                throw e; // 예외를 다시 던져 Spring Batch가 처리하도록 합니다.
            }
        }

    }
}
