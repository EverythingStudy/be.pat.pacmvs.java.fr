package cn.staitech.fr.vo.annotation.in;

import lombok.Data;

@Data
public class StartRecognition {
	private Long imageId;
    private Long slideId;
    private String imageUrl;
    private String algorithm_name;
    private Long organizationId;
	private String organizationName;
}
