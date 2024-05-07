package cn.staitech.fr.vo.annotation;

import cn.staitech.fr.domain.out.MatrixReviewListOut;
import lombok.Data;

@Data
public class AiAlgorithm extends  MatrixReviewListOut{
	private String algorithm_name;
	private Long organizationId;
	private String organizationName;
}
