package cn.staitech.fr.vo.annotation;

import cn.staitech.fr.domain.out.MatrixReviewListOut;
import lombok.Data;

@Data
public class AiAlgorithm extends  MatrixReviewListOut{
	
	public AiAlgorithm(Long singleId,Long slideId,Long categoryId,String imageUrl,Long imageId) {
        super(singleId, slideId, null, null,imageId, null, null, null, null, null, null, null, categoryId, null, null, null,
        		null, null, null, imageUrl);
    }
	
	private String algorithm_name;
	private Long organizationId;
	private String organizationName;
}
