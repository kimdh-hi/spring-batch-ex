# spring-batch-ex

- 기본적인 `Job`, `Step`, `Tasklet`, `Chunk` 생성 및 실행
- `Chunk` 기반 `Step` 생성 후 파라미터 받기 (`@Scope` + `@Value` + `SpEL`)
- `ItemReader` (`FlatFileItemReader`, `JdbcCursorItemReader`, `JpaItemReader`)
- `ItemWriter` (`FlatFileItemWriter`, `JdbcBatchItemWriter`, `JpaItemWriter`)
- `ItemProcessor` (ItemProcessor 직접구헌(필터링), `CompositItemProcessor`)
- `Exception` 처리 (`Skip`, `Retry`)

📌 회원등급 업데이트 배치 구현 ( lab7 )
- 누적 구매금액(totalAmount)에 따른 회원등급 업데이터 배치 처리