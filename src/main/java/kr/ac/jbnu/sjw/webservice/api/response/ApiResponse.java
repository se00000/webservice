package kr.ac.jbnu.sjw.webservice.api.response;

public class ApiResponse<T> {
	private String status;
	private T data;
	private String message;
	
	private ApiResponse(String status, T data, String message) {
		this.status= status;
		this.data = data;
		this.message = message;
	}
	
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>("success", data, null);
	}
	
	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>("error", null, message);
	}
	
	public String getStatus() {return status;}
	public T getData() {return data;}
	public String getMessage() {return message;}
}
