package kr.ac.jbnu.sjw.webservice.api.dto;

public class ArticleDto {
	private Long id;
	private String title;
	private String content;
	private boolean published;
	
	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}
	
	public String getTitle() {return title;}
	public void setTitle(String title) {this.title = title;}
	
	public String getContent() {return content;}
	public void setContent(String content) {this.content = content;}
	
	public boolean isPublished() {return published;}
	public void setPublished(boolean published) {this.published = published;}
}