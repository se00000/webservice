package kr.ac.jbnu.sjw.webservice.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
	
	@Value("${app.maintenance:false}")
	private boolean maintenance;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		
		if (maintenance) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"status\":\"error\",\"data\":null,\"message\":\"서비스 점검 중\"}");
			log.info("[{}] {} -> 503 (점검 모드)", request.getMethod(), request.getRequestURI());
			return;
		}
		
		long start = System.currentTimeMillis();
		
		filterChain.doFilter(request, response);
		
		long time = System.currentTimeMillis() - start;
		log.info("[{}] {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), 
				response.getStatus(), time);
	}
}
