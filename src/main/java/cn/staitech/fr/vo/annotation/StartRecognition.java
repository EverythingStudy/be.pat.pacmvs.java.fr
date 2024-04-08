package cn.staitech.fr.vo.annotation;

import lombok.Data;

@Data
public class StartRecognition {
	private Long imageId;
    private Long slideId;
    private String imageUrl;
    private String algorithm_name;
}
