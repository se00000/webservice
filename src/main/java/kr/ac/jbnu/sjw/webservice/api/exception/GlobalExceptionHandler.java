package kr.ac.jbnu.sjw.webservice.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import kr.ac.jbnu.sjw.webservice.api.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e)
	{
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error("요청 값의 형식이 올바르지 않습니다."));
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error("요청 본문을 읽을 수 없습니다."));
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e)
	{
		if (e instanceof ErrorResponse errorResponse) {
			return ResponseEntity.status(errorResponse.getStatusCode())
					.body(ApiResponse.error("잘못된 요청입니다."));
		}
		
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("서버 처리 중 오류가 발생했습니다."));
	}
}
