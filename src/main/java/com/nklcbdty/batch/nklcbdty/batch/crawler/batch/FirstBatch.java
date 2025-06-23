package com.nklcbdty.batch.nklcbdty.batch.crawler.batch;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.BatchJobMstRepository;
import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.JobMstRepository;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Batch_output_job_mst;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Configuration
@EnableBatchProcessing
@Slf4j
public class FirstBatch {
	private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final JobMstRepository jobMstRepository;
    private final BatchJobMstRepository batchJobMstRepository;
    
	public FirstBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
			JobMstRepository jobMstRepository, BatchJobMstRepository batchJobMstRepository) {

		this.jobRepository = jobRepository;
		this.platformTransactionManager = platformTransactionManager;
		this.jobMstRepository = jobMstRepository;
		this.batchJobMstRepository = batchJobMstRepository;
	}
	
	// OkHttpClient 연결/리드/읽기 타임아웃 1분으로 지정.
	private final OkHttpClient client = new OkHttpClient.Builder()
	        .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃
	        .readTimeout(60, TimeUnit.SECONDS)    // 읽기 타임아웃
	        .writeTimeout(60, TimeUnit.SECONDS)   // 쓰기 타임아웃
	        .build();
    
	// Job_mst Http통신요청부터 배치 시작!
    @Bean
    public Job requestCrawlerJob() {
        return new JobBuilder("requestCrawlerJob", jobRepository) // JobRepository를 생성자에 전달
                .incrementer(new RunIdIncrementer())
                .start(requestStep())
                .build();
    }

    @Bean
    public Step requestStep() {
        // StepBuilder 생성자에 JobRepository와 PlatformTransactionManager를 직접 넘겨줍니다.
        return new StepBuilder("requestStep", jobRepository) // <-- 이 부분이 수정되었습니다!
            .<String, Response>chunk(1, platformTransactionManager) // chunk 메서드에 transactionManager 전달
            .reader(urlReader())
            .processor(httpRequestProcessor())
            .writer(httpResponseWriter())
            .build();
    }

    @Bean
    public ItemReader<String> urlReader() {
    	List<String> urls = List.of(
    		"https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=naver",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=kakao",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=line",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=coupang",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=baemin",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=daangn",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=toss",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=yanolja"
        );

        return new ListItemReader<>(urls);
    }

    @Bean
    public ItemProcessor<String, Response> httpRequestProcessor() {
        return new ItemProcessor<String, Response>() {
            @Override
            public Response process(String url) throws Exception {
                // HTTP 요청을 수행하는 로직
                Response response = performHttpRequest(url);
                return response;
            }

            private Response performHttpRequest(String url) throws IOException {
                // HTTP 요청을 수행하고 응답을 반환하는 로직 구현
                // 예: HttpClient를 사용하여 GET 요청 수행
                // HttpResponse response = httpClient.execute(new HttpGet(url));
                // return response;
            	
            	Request request = new Request.Builder()
                        .url(url)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    // 응답 처리
                } catch (IOException e) {
                	log.info("httpRequestProcessor Request통신중에 에러가 발생했습니다. url = {} , message = {}", url, e.getMessage());
//                    System.err.println("Error fetching data from: " + url + " - " + e.getMessage());
                }
            	return client.newCall(request).execute();
                 // 실제 응답 객체로 대체
            }
        };
    }

    @Bean
    public ItemWriter<Response> httpResponseWriter() {
        return new ItemWriter<Response>() {
            @Override
            public void write(Chunk<? extends Response> chunk) throws Exception {
                for (Response response : chunk.getItems()) { // <-- 이 부분이 변경되었습니다!
                    // 응답을 처리하는 로직
                    // 예: 데이터베이스에 저장하거나 로그에 기록
                    log.info("Response: {}", response);
                }
            }
        };
    }

    ///////////////// requestCrawlerJob step여기까지.
    
    
	
	@Bean
	public Job batchMstCRUDJob() {
        return new JobBuilder("batchMstCRUDJob", jobRepository) // <-- 이 부분이 변경되었습니다!
            // .repository(jobRepository) // 이 라인은 이제 필요 없습니다!
            // .incrementer(new RunIdIncrementer()) // 필요하다면 주석 해제하여 사용
            .start(batchMstDelete())
            .next(firstStep())
            .build();
	}
	
	@Bean
	public Step batchMstDelete() {
        return new StepBuilder("batchMstDelete", jobRepository) // <-- 이 부분이 변경되었습니다!
            .<Batch_output_job_mst, Batch_output_job_mst> chunk(10, platformTransactionManager) // <-- chunk 메서드에 transactionManager 전달!
            .reader(bathchMstReader())
            .writer(bathchMstWriter())
            .build();

	}
	
	@Bean
	public RepositoryItemReader<Batch_output_job_mst> bathchMstReader() {
	    return new RepositoryItemReaderBuilder<Batch_output_job_mst>()
	            .name("beforeReader")
	            .pageSize(10)
	            .methodName("findAll")
	            .repository(batchJobMstRepository)
	            .sorts(Map.of("id", Sort.Direction.ASC))
	            .build();
	}
	
	@Bean
    public ItemWriter<Batch_output_job_mst> bathchMstWriter() {
        return new ItemWriter<Batch_output_job_mst>() {
            @Override
            public void write(Chunk<? extends Batch_output_job_mst> chunk) throws Exception {
                batchJobMstRepository.deleteAll();
            }
        };
    }
	
    @Bean
    public Step firstStep() {
        // StepBuilder 생성자에는 JobRepository만 넘겨줍니다.
        return new StepBuilder("firstStep", jobRepository) // <-- 이 부분이 변경되었습니다!
            .<Job_mst, Batch_output_job_mst> chunk(10, platformTransactionManager) // <-- chunk 메서드에 transactionManager 전달!
            .reader(beforeReader())
            .processor(middleProcessor())
            .writer(afterWriter())
            .build();
    }
	
	
	@Bean
	public RepositoryItemReader<Job_mst> beforeReader() {
	    return new RepositoryItemReaderBuilder<Job_mst>()
	            .name("beforeReader")
	            .pageSize(10)
	            .methodName("findAll")
	            .repository(jobMstRepository)
	            .sorts(Map.of("id", Sort.Direction.ASC))
	            .build();
	}
	
	@Bean
	public ItemProcessor<Job_mst, Batch_output_job_mst> middleProcessor() {
		// 중복된 annoId를 추적하기 위한 Set
	    Set<Long> processedAnnoIds = new HashSet<>();
	    
		return new ItemProcessor<Job_mst, Batch_output_job_mst>() {	
			@Override
	        public Batch_output_job_mst process(Job_mst item) throws Exception {
				
				// 현재 item의 annoId가 이미 처리된 것인지 확인
	            if (processedAnnoIds.contains(item.getAnnoId())) {
	                // 중복된 경우 null 반환하여 후속 처리에서 건너뛰게 함
	                return null;
	            }

	            // 중복이 아닌 경우, annoId를 Set에 추가
	            processedAnnoIds.add(item.getAnnoId());
				
				Batch_output_job_mst afterEntity = new Batch_output_job_mst();
	            afterEntity.setId(item.getId());
	            afterEntity.setCompanyCd(item.getCompanyCd());
	            afterEntity.setAnnoId(item.getAnnoId());
	            afterEntity.setClassCdNm(item.getClassCdNm());
	            afterEntity.setEmpTypeCdNm(item.getEmpTypeCdNm());
	            afterEntity.setAnnoSubject(item.getAnnoSubject());
                    
	            afterEntity.setSubJobCdNm(item.getSubJobCdNm());
	            afterEntity.setSysCompanyCdNm(item.getSysCompanyCdNm());
	            afterEntity.setJobDetailLink(item.getJobDetailLink());
	            afterEntity.setWorkplace(item.getWorkplace());
	            afterEntity.setBatchDate(getTodayDate());
	            
	            return afterEntity;
	        }
		};
	}
	
	@Bean
    public RepositoryItemWriter<Batch_output_job_mst> afterWriter() {

        return new RepositoryItemWriterBuilder<Batch_output_job_mst>()
                .repository(batchJobMstRepository)
                .methodName("save")
                .build();
    }
    
    /**
     * <p>Batch_output_job_mst 엔터티에 데이터 삽입시 batchDate는 현재시간을 추출해서 set한다.</p>
     * @author JaCob LEE
     * */
    private String getTodayDate() {
    	LocalDateTime today = LocalDateTime.now(); // 현재 날짜와 시간으로 설정
    	// 포맷 정의
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return today.format(dateFormat);
    }
	
    
}
