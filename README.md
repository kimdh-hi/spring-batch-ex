# spring-batch-ex

- 기본적인 `Job`, `Step`, `Tasklet`, `Chunk` 생성 및 실행
- `Chunk` 기반 `Step` 생성 후 파라미터 받기 (`@Scope` + `@Value` + `SpEL`)
- `ItemReader` (`FlatFileItemReader`, `JdbcCursorItemReader`, `JpaItemReader`)
- `ItemWriter` (`FlatFileItemWriter`, `JdbcBatchItemWriter`, `JpaItemWriter`)
- `ItemProcessor` (ItemProcessor 직접구헌(필터링), `CompositItemProcessor`)
- `Exception` 처리 (`Skip`, `Retry`)
