package dutchiepay.backend.domain.image.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetPreSignedUrlRequestDto {
    private String fileName;
}
