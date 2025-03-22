package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.chat.entity.Report;
import com.example.ssauc.user.chat.repository.ReportRepository;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    void testSaveAndFindReport() {
        // 신고자와 신고 대상 유저 생성
        Users reporter = Users.builder()
                .userName("reporter")
                .email("reporter@example.com")
                .password("pass1")
                .createdAt(LocalDateTime.now())
                .build();
        Users reported = Users.builder()
                .userName("reported")
                .email("reported@example.com")
                .password("pass2")
                .createdAt(LocalDateTime.now())
                .build();
        reporter = usersRepository.save(reporter);
        reported = usersRepository.save(reported);

        // Report 엔티티 생성 및 저장
        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reported)
                .reportReason("불건전한 내용")
                .status("접수")
                .details("상세 신고 내용")
                .reportDate(LocalDateTime.now())
                .build();

        Report saved = reportRepository.save(report);

        // 저장된 Report 조회 및 검증
        Report found = reportRepository.findById(saved.getReportId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getReportReason()).isEqualTo("불건전한 내용");
        assertThat(found.getStatus()).isEqualTo("접수");
    }
}
