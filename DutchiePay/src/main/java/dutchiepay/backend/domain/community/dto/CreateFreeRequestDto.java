package dutchiepay.backend.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateFreeRequestDto {

    @NotBlank(message = "제목이 입력되지 않았습니다.")
    private String title;
    private String content;
    @NotBlank(message = "카테고리가 지정되지 않았습니다.")
    private String category;
    @NotBlank(message = "썸네일이 지정되지 않았습니다.")
    private String thumbnail;
}
