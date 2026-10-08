package kr.ac.jbnu.sjw.webservice.api.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.jbnu.sjw.webservice.api.dto.ArticleDto;
import kr.ac.jbnu.sjw.webservice.api.request.ArticleCreateRequest;
import kr.ac.jbnu.sjw.webservice.api.request.ArticleUpdateRequest;
import kr.ac.jbnu.sjw.webservice.api.response.ApiResponse;

@RestController
@RequestMapping("/api/v3/articles")

public class ArticleController3 {
	private final Map<Long, ArticleDto> store = new HashMap<>();
	private long sequence = 1L;
	
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ArticleDto>> getArticle(@PathVariable Long id) {
		ArticleDto article = store.get(id);
		if (article == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error("기사를 찾을 수 없습니다."));
		}
		
		return ResponseEntity.ok(ApiResponse.success(article));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ArticleDto>>> getArticles(
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size)
	{
		List<ArticleDto> result= store.values().stream()
				.filter(article->keyword == null || article.getTitle().contains(keyword))
				.skip((long)page * size)
				.limit(size)
				.toList();
		
		return ResponseEntity.ok(ApiResponse.success(result));
	}
	
	@GetMapping("/error-test")
	public ResponseEntity<ApiResponse<Void>> errorTest() {
		throw new RuntimeException("테스트용 서버 에러");
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<ArticleDto>> createArticle(@RequestBody ArticleCreateRequest request) {
		
		if (request.getTitle() == null || request.getTitle().isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("제목을 입력하세요."));
		}
		
		ArticleDto article = new ArticleDto();
		article.setId(sequence++);
		article.setTitle(request.getTitle());
		article.setContent(request.getContent());
		
		store.put(article.getId(), article);
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(article));
	}
	
	@PostMapping("/with-header")
	public ResponseEntity<ApiResponse<String>> createArticleWithHeader(
			@RequestBody ArticleCreateRequest request,
			@RequestHeader(value = "X-USER-ID", required = false) String userId,
			@RequestHeader(value = "Authorization", required = false) String authorization)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("요청한 기사: ").append(request.getTitle())
		.append(", content=").append(request.getContent()).append("\n")
		.append("X-USER-ID: ").append(userId).append("\n")
		.append("Authorization: ").append(authorization).append("\n");
		
		return ResponseEntity.ok(ApiResponse.success(sb.toString()));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ArticleDto>> updateArticle(
			@PathVariable Long id,
			@RequestBody ArticleUpdateRequest request) 
	{
		ArticleDto article = store.get(id);
		if (article == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error("기사를 찾을 수 없습니다."));
		}
		
		if (request.getTitle() != null) {
			article.setTitle(request.getTitle());
		}
		
		if (request.getContent() != null) {
			article.setContent(request.getContent());
		}
		
		return ResponseEntity.ok(ApiResponse.success(article));
	}
	
	@PutMapping("/{id}/publish")
	public ResponseEntity<ApiResponse<ArticleDto>> publishArticle(
			@PathVariable Long id) {
		ArticleDto article = store.get(id);
		
		if (article == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error("기사를 찾을 수 없습니다."));
		}
		
		if (article.isPublished()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("이미 발행된 기사입니다."));
		}
		
		article.setPublished(true);
		
		return ResponseEntity.ok(ApiResponse.success(article));
	}
	
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteArticle(@PathVariable Long id) {
		ArticleDto removed = store.remove(id);
		
		if (removed == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error("기사를 찾을 수 없습니다."));
		}
		
		return ResponseEntity.ok(ApiResponse.success("삭제되었습니다."));
	}
	
	@DeleteMapping("/drafts")
	public ResponseEntity<ApiResponse<String>> deleteDrafts() {
		boolean removed = store.values().removeIf(article -> !article.isPublished());
		
		if (!removed) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error("발행 전 기사가 없습니다."));
		}
		
		return ResponseEntity.ok(ApiResponse.success("삭제되었습니다."));
	}
}
